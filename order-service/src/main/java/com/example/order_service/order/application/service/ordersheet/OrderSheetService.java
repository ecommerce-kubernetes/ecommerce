package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.application.port.*;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.config.OrderSheetProperties;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetService {
    private final OrderSheetProperties orderSheetProperties;
    private final OrderProductPort orderProductPort;
    private final OrderCouponPort orderCouponPort;
    private final OrderUserPort orderUserPort;
    private final OrderCartPort orderCartPort;
    private final OrderValidator orderValidator;
    private final PointUsagePolicy pointUsagePolicy;
    private final OrderSheetContextFactory contextFactory;
    private final OrderSheetRepository repository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public OrderSheetCreateResult createCartOrderSheet(CreateCartOrderSheetCommand command) {
        OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(command.userId());
        OrderCartItemsResult cartItems = orderCartPort.getCartItems(command.userId(), command.cartItemIds());
        orderValidator.validateMissingCartItems(command.cartItemIds(), cartItems);

        List<Long> orderVariantIds = cartItems.toProductVariantIds();
        OrderProductsResult products = orderProductPort.getProducts(orderVariantIds);
        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        cartItems.items().forEach(item ->
                orderValidator.validateOrderable(productsMap.get(item.productVariantId()), item.quantity()));

        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(orderSheetProperties.ttlMinutes());
        CreateOrderSheetContext createContext = contextFactory.createForCart(ordererProfile, cartItems, products, expiresAt);

        OrderSheet orderSheet = OrderSheet.create(createContext, idGenerator);
        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));

        return OrderSheetCreateResult.from(savedOrderSheet);
    }

    public OrderSheetCreateResult createDirectOrderSheet(CreateDirectOrderSheetCommand command) {
        OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(command.userId());

        List<Long> orderVariantIds = command.toItemVariantIds();
        OrderProductsResult products = orderProductPort.getProducts(orderVariantIds);
        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        command.items().forEach(item ->
                orderValidator.validateOrderable(productsMap.get(item.productVariantId()), item.quantity()));

        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(orderSheetProperties.ttlMinutes());
        CreateOrderSheetContext directContext = contextFactory.createForDirect(ordererProfile, command, products, expiresAt);

        OrderSheet orderSheet = OrderSheet.create(directContext, idGenerator);
        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetCreateResult.from(savedOrderSheet);
    }

    public OrderSheetResult getOrderSheet(Long orderSheetId, Long userId) {
        OrderSheet orderSheet = getValidOrderSheet(orderSheetId, userId);

        OrdererPointResult ordererPoints = orderUserPort.getOrdererPoints(userId);
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);

        return OrderSheetResult.of(orderSheet, ordererPoints.availablePoints(), maxUsablePoints);
    }

    public OrderSheetUpdateResult updateShippingAddress(UpdateOrderSheetShippingAddressCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        ShippingAddress shippingAddress = ShippingAddress.of(command.receiverName(), command.receiverPhone(),
                command.zipCode(), command.address(), command.addressDetail());
        orderSheet.changeShippingAddress(shippingAddress);

        Duration remainingTtl = orderSheet.calculateRemainingTtl(LocalDateTime.now(clock));
        OrderSheet savedOrderSheet = repository.save(orderSheet, remainingTtl);

        return OrderSheetUpdateResult.of(savedOrderSheet.getId(), orderSheet.getExpiresAt());
    }

    public OrderSheetUpdateResult applyItemCoupons(ApplyItemCouponsCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());
        LocalDateTime currentTime = LocalDateTime.now(clock);

        ItemCouponsResult itemCouponsResult = orderCouponPort.getItemCoupons(command.userId(), command.toItemCouponIds());
        Map<Long, ItemCouponsResult.ItemCouponResult> itemCouponMap = itemCouponsResult.toMap();

        for (ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand : command.itemCouponCommands()) {
            ItemCouponsResult.ItemCouponResult couponResult = itemCouponMap.get(itemCouponCommand.itemCouponId());
            orderValidator.validateItemCoupon(couponResult, currentTime);
            orderSheet.applyItemCoupon(itemCouponCommand.orderSheetItemId(), couponResult.itemCoupon(), pointUsagePolicy);
        }

        Duration remainingTtl = orderSheet.calculateRemainingTtl(currentTime);
        OrderSheet savedOrderSheet = repository.save(orderSheet, remainingTtl);

        return OrderSheetUpdateResult.of(savedOrderSheet.getId(), orderSheet.getExpiresAt());
    }

    public OrderSheetUpdateResult applyCartCoupon(ApplyCartCouponCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());
        LocalDateTime currentTime = LocalDateTime.now(clock);
        CartCouponResult cartCouponResult = orderCouponPort.getCartCoupon(command.userId(), command.cartCouponId());

        orderValidator.validateCartCoupon(cartCouponResult, currentTime);

        orderSheet.applyCartCoupon(cartCouponResult.cartCoupon(), pointUsagePolicy);

        Duration remainingTtl = orderSheet.calculateRemainingTtl(currentTime);
        OrderSheet savedOrderSheet = repository.save(orderSheet, remainingTtl);

        return OrderSheetUpdateResult.of(savedOrderSheet.getId(), orderSheet.getExpiresAt());
    }

    public OrderSheetUpdateResult applyPoints(ApplyPointCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(command.userId());
        Money usedPoints = Money.wons(command.usedPoints());

        orderValidator.validateAvailablePoints(ordererProfile.availablePoints(), usedPoints);

        orderSheet.applyPoints(usedPoints, pointUsagePolicy);

        Duration remainingTtl = orderSheet.calculateRemainingTtl(LocalDateTime.now(clock));
        OrderSheet savedOrderSheet = repository.save(orderSheet, remainingTtl);

        return OrderSheetUpdateResult.of(savedOrderSheet.getId(), orderSheet.getExpiresAt());
    }

    private OrderSheet getValidOrderSheet(Long orderSheetId, Long userId) {
        OrderSheet orderSheet = repository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }
}
