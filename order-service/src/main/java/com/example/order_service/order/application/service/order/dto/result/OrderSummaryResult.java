package com.example.order_service.order.application.service.order.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderItem;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSummaryResult(
        Long orderId,
        OrderStatus status,
        List<OrderItemResult> orderItems,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {

    @Builder
    public record OrderItemResult(
            Long orderItemId,
            ProductSnapshot product,
            List<ProductOptionSnapshot> options,
            Integer quantity,
            OrderItemAmount orderItemAmount
    ) {

        public static OrderItemResult from(OrderItem orderItem) {
            return OrderItemResult.builder()
                    .orderItemId(orderItem.getId())
                    .product(orderItem.getProduct())
                    .options(orderItem.getOptions())
                    .quantity(orderItem.getQuantity())
                    .orderItemAmount(orderItem.getOrderItemAmount())
                    .build();
        }

        public static List<OrderItemResult> from(List<OrderItem> orderItems) {
            return orderItems.stream().map(OrderItemResult::from).toList();
        }
    }

    public static OrderSummaryResult from(Order order) {
        return OrderSummaryResult.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .orderItems(OrderItemResult.from(order.getOrderItems()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
