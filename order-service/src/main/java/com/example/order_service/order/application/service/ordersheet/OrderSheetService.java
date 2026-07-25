package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.OrderUserPort;
import com.example.order_service.order.application.port.dto.result.*;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.application.port.OrderSheetRepository;
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
    private final OrderProductPort orderProductAdaptor;
    private final OrderCouponPort orderCouponAdaptor;
    private final OrderUserPort orderUserAdaptor;
    private final PointUsagePolicy pointUsagePolicy;
    private final OrderSheetRepository repository;
    private final Clock clock;

    public OrderSheetCreateResult createCartOrderSheet(CreateCartOrderSheetCommand command) {
        return null;
    }

    public OrderSheetCreateResult createDirectOrderSheet(CreateDirectOrderSheetCommand command) {
        OrdererProfileResult ordererProfile = orderUserAdaptor.getOrdererProfile(command.userId());

        List<Long> orderVariantIds = command.toItemVariantIds();
        OrderProductResult products = orderProductAdaptor.getProducts(orderVariantIds);

        Map<Long, OrderProductResult.OrderProductDetail> productsMap = products.getProductsMap();
        List<OrderSheetItem> orderSheetItems = createOrderSheetItems(command, productsMap);

        OrderSheet orderSheet = createOrderSheet(ordererProfile, orderSheetItems);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetCreateResult.from(savedOrderSheet);
    }

    private OrderSheet createOrderSheet(OrdererProfileResult ordererProfile, List<OrderSheetItem> orderSheetItems) {
        OrderSheet orderSheet = OrderSheet.create(ordererProfile.orderer(), orderSheetItems,
                LocalDateTime.now(clock).plusMinutes(orderSheetProperties.ttlMinutes()));

        if (ordererProfile.defaultShippingAddress() != null) {
            orderSheet.changeShippingAddress(ordererProfile.defaultShippingAddress());
        }
        return orderSheet;
    }

    private List<OrderSheetItem> createOrderSheetItems(CreateDirectOrderSheetCommand command,
                                                       Map<Long, OrderProductResult.OrderProductDetail> productsMap) {
        return command.items().stream().map(orderVariant -> {
            OrderProductResult.OrderProductDetail product = productsMap.get(orderVariant.productVariantId());
            validateProductIsOrderable(product, orderVariant.quantity());
            return OrderSheetItem.create(product.productSnapshot(), product.priceSnapshot(),
                    orderVariant.quantity(), product.options());
        }).toList();
    }

    private void validateProductIsOrderable(OrderProductResult.OrderProductDetail product, int quantity) {
        if (product == null) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        if (product.status() != OrderProductStatus.ON_SALE) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
        }

        if (quantity > product.stock()) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
        }
    }

    public OrderSheetResult getOrderSheet(String orderSheetId, Long userId) {
        OrderSheet orderSheet = getValidOrderSheet(orderSheetId, userId);

        OrdererPointResult ordererPoints = orderUserAdaptor.getOrdererPoints(userId);
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);

        return OrderSheetResult.of(orderSheet, ordererPoints.availablePoints(), maxUsablePoints);
    }

    public OrderSheetResult updateShippingAddress(UpdateOrderSheetShippingAddressCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        ShippingAddress shippingAddress = ShippingAddress.of(command.receiverName(), command.receiverPhone(),
                command.zipCode(), command.address(), command.addressDetail());
        orderSheet.changeShippingAddress(shippingAddress);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));

        OrdererPointResult ordererPoints = orderUserAdaptor.getOrdererPoints(command.userId());
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);
        return OrderSheetResult.of(savedOrderSheet, ordererPoints.availablePoints(), maxUsablePoints);
    }

    public OrderSheetResult applyItemCoupon(ApplyItemCouponCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        ItemCouponResult itemCouponResult = orderCouponAdaptor.getItemCoupon(command.userId(), command.itemCouponId());

        orderSheet.applyItemCoupon(command.orderSheetItemId(), itemCouponResult.itemCoupon(), pointUsagePolicy);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));

        OrdererPointResult ordererPoints = orderUserAdaptor.getOrdererPoints(command.userId());
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);
        return OrderSheetResult.of(savedOrderSheet, ordererPoints.availablePoints(), maxUsablePoints);
    }

    public OrderSheetResult applyCartCoupon(ApplyCartCouponCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        CartCouponResult cartCouponResult = orderCouponAdaptor.getCartCoupon(command.userId(), command.cartCouponId());

        orderSheet.applyCartCoupon(cartCouponResult.cartCoupon(), pointUsagePolicy);

        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));

        OrdererPointResult ordererPoints = orderUserAdaptor.getOrdererPoints(command.userId());
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);
        return OrderSheetResult.of(savedOrderSheet, ordererPoints.availablePoints(), maxUsablePoints);
    }

    public OrderSheetResult applyPoints(ApplyPointCommand command) {
        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        OrdererProfileResult ordererProfile = orderUserAdaptor.getOrdererProfile(command.userId());
        Money usedPoints = Money.wons(command.usedPoints());

        if (ordererProfile.availablePoints().isLessThan(usedPoints)) {
            throw new BusinessException(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
        }

        orderSheet.applyPoints(usedPoints, pointUsagePolicy);
        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));

        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointUsagePolicy);
        return OrderSheetResult.of(savedOrderSheet, ordererProfile.availablePoints(), maxUsablePoints);
    }

    private OrderSheet getValidOrderSheet(String orderSheetId, Long userId) {
        OrderSheet orderSheet = repository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }
}
