package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
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

        OrderProductsResult products = getProducts(orderSheet);
        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        for (OrderSheetItem item : orderSheet.getItems()) {
            OrderProductsResult.OrderProductDetail product = productsMap.get(item.getProductVariantId());
            orderValidator.validateOrderable(product, item.getQuantity());
            item.validatePriceNotChanged(product.priceSnapshot());
        }

        List<OrderSheetItem> items = orderSheet.findOrderSheetItemsWithAppliedItemCoupon();
        if (!items.isEmpty()) {
            List<Long> itemCouponIds = items.stream().map(item -> item.getItemCouponSnapshot().getItemCouponId()).toList();
            ItemCouponsResult itemCoupons = orderCouponPort.getItemCoupons(command.userId(), itemCouponIds);
            Map<Long, ItemCouponsResult.ItemCouponResult> couponMap = itemCoupons.toMap();

            for (OrderSheetItem item : items) {
                ItemCouponsResult.ItemCouponResult itemCoupon = couponMap.get(item.getItemCouponSnapshot().getItemCouponId());
                orderValidator.validateItemCoupon(itemCoupon, currentTime);
                item.validateItemCouponNotChanged(itemCoupon.itemCoupon());
            }
        }

        if (orderSheet.hasCoupon()) {
            Long cartCouponId = orderSheet.getCartCoupon().getCartCouponId();
            CartCouponResult cartCoupon = orderCouponPort.getCartCoupon(command.userId(), cartCouponId);
            orderValidator.validateCartCoupon(cartCoupon, currentTime);
            orderSheet.validateCartCouponNotChanged(cartCoupon.cartCoupon());
        }

        if (!orderSheet.getUsedPoints().equals(Money.ZERO)) {
            OrdererProfileResult ordererProfile = orderUserPort.getOrdererProfile(command.userId());
            orderValidator.validateAvailablePoints(ordererProfile.availablePoints(), orderSheet.getUsedPoints());
            orderSheet.validatePointsLimit(orderSheet.getUsedPoints(), pointUsagePolicy);
        }

        List<CreateOrderItemContext> orderItemCtx = orderSheet.getItems().stream().map(item -> {
            OrderItemAmount orderItemAmount = OrderItemAmount.of(
                    item.calculateOriginalLineTotal(),
                    item.calculateItemDiscountLineTotal(),
                    item.calculateLineTotal(),
                    item.calculateCouponDiscount(),
                    item.calculateFinalAmount()
            );
            CreateOrderItemContext.CreateOrderItemContextBuilder builder = CreateOrderItemContext.builder()
                    .productSnapshot(item.getProductSnapshot())
                    .priceSnapshot(item.getPriceSnapshot())
                    .quantity(item.getQuantity())
                    .options(item.getOptionSnapshots())
                    .orderItemAmount(orderItemAmount);

            if (item.hasCoupon()) {
                AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(item.getItemCouponSnapshot().getItemCouponId(),
                        item.getItemCouponSnapshot().getName());
                return builder
                        .appliedItemCoupon(appliedItemCoupon)
                        .build();
            }
            return builder
                    .appliedItemCoupon(null)
                    .build();
        }).toList();

        OrderAmount orderAmount = OrderAmount.of(orderSheet.calculateTotalOriginalAmount(), orderSheet.calculateTotalItemDiscount(),
                orderSheet.calculateTotalItemCouponDiscount(), orderSheet.calculateCartCouponDiscount(), orderSheet.getUsedPoints(),
                orderSheet.calculateTotalPaymentAmount());
        CreateOrderContext.CreateOrderContextBuilder builder = CreateOrderContext.builder()
                .orderer(orderSheet.getOrderer())
                .shippingAddress(orderSheet.getShippingAddress())
                .items(orderItemCtx)
                .orderAmount(orderAmount);
        if (orderSheet.hasCoupon()) {
            AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(orderSheet.getCartCoupon().getCartCouponId(), orderSheet.getCartCoupon().getName());
            builder.appliedCartCoupon(appliedCartCoupon);
        } else {
            builder.appliedCartCoupon(null).build();
        }
        CreateOrderContext context = builder.build();

        Long orderId = orderCommandService.saveOrder(context);

        return OrderCreateResult.builder()
                .orderId(orderId)
                .build();
    }

    private OrderSheet getValidOrderSheet(Long orderSheetId, Long userId) {
        OrderSheet orderSheet = orderSheetRepository.findByIdAndOrdererId(orderSheetId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_NOT_FOUND));

        if (orderSheet.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(OrderErrorCode.ORDER_SHEET_EXPIRED);
        }
        return orderSheet;
    }

    private OrderProductsResult getProducts(OrderSheet orderSheet) {
        List<Long> productVariantIds = orderSheet.getItems().stream()
                .map(OrderSheetItem::getProductVariantId).toList();
        return orderProductPort.getProducts(productVariantIds);
    }

    private OrderSheet findOrderSheetById(String sheetId) {
        return null;
    }
}
