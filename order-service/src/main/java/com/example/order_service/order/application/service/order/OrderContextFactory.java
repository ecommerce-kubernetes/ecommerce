package com.example.order_service.order.application.service.order;

import com.example.order_service.order.domain.order.AppliedCartCoupon;
import com.example.order_service.order.domain.order.AppliedItemCoupon;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderContextFactory {

    public CreateOrderContext create(OrderSheet orderSheet) {
        OrderAmount orderAmount = OrderAmount.of(orderSheet.calculateTotalOriginalAmount(), orderSheet.calculateTotalItemDiscount(),
                orderSheet.calculateTotalItemCouponDiscount(), orderSheet.calculateCartCouponDiscount(), orderSheet.getUsedPoints(),
                orderSheet.calculateTotalPaymentAmount());

        List<CreateOrderItemContext> orderItemContexts = createOrderItemContexts(orderSheet.getItems());

        CreateOrderContext.CreateOrderContextBuilder builder = CreateOrderContext.builder()
                .orderer(orderSheet.getOrderer())
                .shippingAddress(orderSheet.getShippingAddress())
                .items(orderItemContexts)
                .orderAmount(orderAmount);

        if (orderSheet.hasCoupon()) {
            AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(
                    orderSheet.getCartCoupon().getCartCouponId(),
                    orderSheet.getCartCoupon().getName()
            );
            builder.appliedCartCoupon(appliedCartCoupon);
        }
        return builder.build();
    }

    private List<CreateOrderItemContext> createOrderItemContexts(List<OrderSheetItem> orderSheetItems) {
        return orderSheetItems.stream().map(item -> {
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
                builder.appliedItemCoupon(appliedItemCoupon);
            }
            return builder.build();
        }).toList();
    }
}
