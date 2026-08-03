package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.OrderUserPort;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.domain.order.AppliedCartCoupon;
import com.example.order_service.order.domain.order.AppliedItemCoupon;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderSheetRepository orderSheetRepository;
    private final OrderCommandService orderCommandService;
    private final OrderProductPort orderProductPort;
    private final OrderCouponPort orderCouponPort;
    private final OrderUserPort orderUserPort;
    private final PointUsagePolicy pointUsagePolicy;
    private final OrderValidator orderValidator;
    private final Clock clock;

    public OrderCreateResult createOrder(CreateOrderCommand command) {
        LocalDateTime currentTime = LocalDateTime.now(clock);

        OrderSheet orderSheet = getValidOrderSheet(command.orderSheetId(), command.userId());

        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = validateOrderItems(orderSheet);

        Map<Long, ItemCouponsResult.ItemCouponResult> itemCouponsMap = validateItemCoupons(orderSheet, currentTime);

        CartCouponResult cartCouponResult = validateCartCoupon(orderSheet, currentTime);

        validateUsedPoints(orderSheet);

        CreateOrderContext orderContext = createOrderContext(orderSheet, productsMap, itemCouponsMap, cartCouponResult);

        Long orderId = orderCommandService.saveOrder(orderContext);

        return OrderCreateResult.of(orderId);
    }

    private Map<Long, OrderProductsResult.OrderProductDetail> validateOrderItems(OrderSheet orderSheet) {
        List<Long> productVariantIds = orderSheet.getItems().stream()
                .map(OrderSheetItem::getProductVariantId).toList();
        OrderProductsResult products = orderProductPort.getProducts(productVariantIds);

        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        for (OrderSheetItem item : orderSheet.getItems()) {
            OrderProductsResult.OrderProductDetail product = productsMap.get(item.getProductVariantId());
            orderValidator.validateOrderable(product, item.getQuantity());
            item.validatePriceNotChanged(product.priceSnapshot());
        }
        return productsMap;
    }

    private Map<Long, ItemCouponsResult.ItemCouponResult> validateItemCoupons(OrderSheet orderSheet, LocalDateTime currentTime) {
        List<OrderSheetItem> appliedItemCouponItems = orderSheet.findOrderSheetItemsWithAppliedItemCoupon();
        if (appliedItemCouponItems.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> itemCouponIds = appliedItemCouponItems.stream().map(item -> item.getItemCouponSnapshot().getItemCouponId()).toList();
        ItemCouponsResult itemCoupons = orderCouponPort.getItemCoupons(orderSheet.getOrderer().getUserId(), itemCouponIds);
        Map<Long, ItemCouponsResult.ItemCouponResult> itemCouponsMap = itemCoupons.toMap();
        for (OrderSheetItem item : appliedItemCouponItems) {
            ItemCouponsResult.ItemCouponResult itemCoupon = itemCouponsMap.get(item.getItemCouponSnapshot().getItemCouponId());
            orderValidator.validateItemCoupon(itemCoupon, currentTime);
            item.validateItemCouponNotChanged(itemCoupon.itemCoupon());
        }

        return itemCouponsMap;
    }

    private CartCouponResult validateCartCoupon(OrderSheet orderSheet, LocalDateTime currentTime) {
        if (!orderSheet.hasCoupon()){
            return null;
        }
        Long cartCouponId = orderSheet.getCartCoupon().getCartCouponId();
        CartCouponResult cartCouponResult = orderCouponPort.getCartCoupon(orderSheet.getOrderer().getUserId(), cartCouponId);
        orderValidator.validateCartCoupon(cartCouponResult, currentTime);
        orderSheet.validateCartCouponNotChanged(cartCouponResult.cartCoupon());

        return cartCouponResult;
    }

    private void validateUsedPoints(OrderSheet orderSheet) {
        if (orderSheet.getUsedPoints().equals(Money.ZERO)) {
            return;
        }
        OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(orderSheet.getOrderer().getUserId());
        orderValidator.validateAvailablePoints(ordererProfile.availablePoints(), orderSheet.getUsedPoints());
        orderSheet.validatePointsLimit(orderSheet.getUsedPoints(), pointUsagePolicy);
    }

    private CreateOrderContext createOrderContext(OrderSheet orderSheet, Map<Long, OrderProductsResult.OrderProductDetail> productsMap,
                                                  Map<Long, ItemCouponsResult.ItemCouponResult> itemCouponsMap, CartCouponResult cartCouponResult) {
        OrderAmount orderAmount = OrderAmount.of(orderSheet.calculateTotalOriginalAmount(), orderSheet.calculateTotalItemDiscount(),
                orderSheet.calculateTotalItemCouponDiscount(), orderSheet.calculateCartCouponDiscount(), orderSheet.getUsedPoints(),
                orderSheet.calculateTotalPaymentAmount());

        List<CreateOrderItemContext> orderItemContexts = createOrderItemContexts(orderSheet.getItems(), productsMap, itemCouponsMap);

        CreateOrderContext.CreateOrderContextBuilder builder = CreateOrderContext.builder()
                .orderer(orderSheet.getOrderer())
                .shippingAddress(orderSheet.getShippingAddress())
                .items(orderItemContexts)
                .orderAmount(orderAmount);
        if (cartCouponResult != null) {
            AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(
                    cartCouponResult.cartCoupon().getCartCouponId(),
                    cartCouponResult.cartCoupon().getName()
            );
            builder.appliedCartCoupon(appliedCartCoupon);
        }
        return builder.build();
    }

    private List<CreateOrderItemContext> createOrderItemContexts(List<OrderSheetItem> orderSheetItems, Map<Long, OrderProductsResult.OrderProductDetail> productsMap,
                                                                 Map<Long, ItemCouponsResult.ItemCouponResult> itemCouponsMap) {
        return orderSheetItems.stream().map(item -> {
            OrderItemAmount orderItemAmount = OrderItemAmount.of(
                    item.calculateOriginalLineTotal(),
                    item.calculateItemDiscountLineTotal(),
                    item.calculateLineTotal(),
                    item.calculateCouponDiscount(),
                    item.calculateFinalAmount()
            );

            OrderProductsResult.OrderProductDetail product = productsMap.get(item.getProductVariantId());
            CreateOrderItemContext.CreateOrderItemContextBuilder builder = CreateOrderItemContext.builder()
                    .productSnapshot(product.productSnapshot())
                    .priceSnapshot(item.getPriceSnapshot())
                    .quantity(item.getQuantity())
                    .options(product.options())
                    .orderItemAmount(orderItemAmount);

            if (item.hasCoupon()) {
                ItemCouponsResult.ItemCouponResult latestItemCoupon = itemCouponsMap.get(item.getItemCouponSnapshot().getItemCouponId());
                AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(latestItemCoupon.itemCoupon().getItemCouponId(),
                        latestItemCoupon.itemCoupon().getName());
                builder.appliedItemCoupon(appliedItemCoupon);
            }
            return builder.build();
        }).toList();
    }

    private OrderSheet getValidOrderSheet(Long orderSheetId, Long userId) {
        OrderSheet orderSheet = orderSheetRepository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }
}
