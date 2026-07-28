package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.application.port.*;
import com.example.order_service.order.application.port.dto.result.*;
import com.example.order_service.order.application.service.OrderValidator;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
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
        List<OrderSheetItem> orderSheetItems = createOrderSheetItems(cartItems, productsMap);

        OrderSheet orderSheet = createOrderSheet(ordererProfile, orderSheetItems);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetCreateResult.from(savedOrderSheet);
    }

    private List<OrderSheetItem> createOrderSheetItems(OrderCartItemsResult cartItems,
                                                       Map<Long, OrderProductsResult.OrderProductDetail> productsMap) {
        return cartItems.items().stream().map(cartItem -> createOrderSheetItem(
                cartItem.productVariantId(),
                cartItem.quantity(),
                productsMap
        )).toList();
    }

    public OrderSheetCreateResult createDirectOrderSheet(CreateDirectOrderSheetCommand command) {
        OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(command.userId());

        List<Long> orderVariantIds = command.toItemVariantIds();
        OrderProductsResult products = orderProductPort.getProducts(orderVariantIds);

        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();
        List<OrderSheetItem> orderSheetItems = createDirectOrderSheetItems(command, productsMap);

        OrderSheet orderSheet = createOrderSheet(ordererProfile, orderSheetItems);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetCreateResult.from(savedOrderSheet);
    }

    private OrderSheet createOrderSheet(OrdererProfileResult ordererProfile, List<OrderSheetItem> orderSheetItems) {
        OrderSheet orderSheet = OrderSheet.create(ordererProfile.orderer(), orderSheetItems,
                LocalDateTime.now(clock).plusMinutes(orderSheetProperties.ttlMinutes()), idGenerator);

        if (ordererProfile.defaultShippingAddress() != null) {
            orderSheet.changeShippingAddress(ordererProfile.defaultShippingAddress());
        }
        return orderSheet;
    }

    private List<OrderSheetItem> createDirectOrderSheetItems(CreateDirectOrderSheetCommand command,
                                                             Map<Long, OrderProductsResult.OrderProductDetail> productsMap) {
        return command.items().stream().map(orderVariant -> createOrderSheetItem(
                orderVariant.productVariantId(),
                orderVariant.quantity(),
                productsMap
        )).toList();
    }

    private OrderSheetItem createOrderSheetItem(Long productVariantId, int quantity,
                                                      Map<Long, OrderProductsResult.OrderProductDetail> productsMap) {
        OrderProductsResult.OrderProductDetail product = productsMap.get(productVariantId);

        orderValidator.validateOrderable(product, quantity);

        return OrderSheetItem.create(
                product.productSnapshot(),
                product.priceSnapshot(),
                quantity,
                product.options(),
                idGenerator
        );
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

    public OrderSheetUpdateResult applyItemCoupon(ApplyItemCouponCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        ItemCouponResult itemCouponResult = orderCouponPort.getItemCoupon(command.userId(), command.itemCouponId());

        orderSheet.applyItemCoupon(command.orderSheetItemId(), itemCouponResult.itemCoupon(), pointUsagePolicy);

        Duration remainingTtl = orderSheet.calculateRemainingTtl(LocalDateTime.now(clock));
        OrderSheet savedOrderSheet = repository.save(orderSheet, remainingTtl);

        return OrderSheetUpdateResult.of(savedOrderSheet.getId(), orderSheet.getExpiresAt());
    }

    public OrderSheetUpdateResult applyCartCoupon(ApplyCartCouponCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        CartCouponResult cartCouponResult = orderCouponPort.getCartCoupon(command.userId(), command.cartCouponId());

        orderSheet.applyCartCoupon(cartCouponResult.cartCoupon(), pointUsagePolicy);

        Duration remainingTtl = orderSheet.calculateRemainingTtl(LocalDateTime.now(clock));
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
