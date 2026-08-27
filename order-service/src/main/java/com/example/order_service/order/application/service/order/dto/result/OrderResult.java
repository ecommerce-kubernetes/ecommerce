package com.example.order_service.order.application.service.order.dto.result;

import com.example.order_service.order.domain.order.*;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResult(
        Long orderId,
        OrderStatus status,
        String orderName,
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<OrderItemResult> orderItems,
        AppliedCartCoupon appliedCartCoupon,
        OrderAmount orderAmount,

        LocalDateTime createdAt
) {

    @Builder
    public record OrderItemResult(
            Long orderItemId,
            ProductSnapshot product,
            ProductPriceSnapshot productPrice,
            List<ProductOptionSnapshot> options,
            AppliedItemCoupon appliedItemCoupon,
            int quantity,
            OrderItemAmount orderItemAmount
    ) {
        public static OrderItemResult from(OrderItem orderItem) {
            return OrderItemResult.builder()
                    .orderItemId(orderItem.getId())
                    .product(orderItem.getProduct())
                    .productPrice(orderItem.getProductPrice())
                    .options(orderItem.getOptions())
                    .appliedItemCoupon(orderItem.getAppliedItemCoupon())
                    .quantity(orderItem.getQuantity())
                    .orderItemAmount(orderItem.getOrderItemAmount())
                    .build();
        }

        public static List<OrderItemResult> from(List<OrderItem> orderItems) {
            return orderItems.stream().map(OrderItemResult::from).toList();
        }
    }

    public static OrderResult from(Order order) {
        return OrderResult.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .orderName(order.getOrderName())
                .orderer(order.getOrderer())
                .shippingAddress(order.getShippingAddress())
                .orderItems(OrderItemResult.from(order.getOrderItems()))
                .appliedCartCoupon(order.getAppliedCartCoupon())
                .orderAmount(order.getOrderAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
