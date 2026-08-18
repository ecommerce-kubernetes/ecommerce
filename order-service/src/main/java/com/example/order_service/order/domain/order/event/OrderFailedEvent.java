package com.example.order_service.order.domain.order.event;

import com.example.order_service.order.domain.order.Order;
import lombok.Builder;

@Builder
public record OrderFailedEvent(
        Long orderId,
        Long userId
) {

    public static OrderFailedEvent from(Order order) {
        return OrderFailedEvent.builder()
                .orderId(order.getId())
                .userId(order.getOrderer().getUserId())
                .build();
    }
}
