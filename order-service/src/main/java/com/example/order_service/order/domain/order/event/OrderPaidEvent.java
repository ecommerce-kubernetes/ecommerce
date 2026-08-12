package com.example.order_service.order.domain.order.event;

import lombok.Builder;

@Builder
public record OrderPaidEvent(
        Long orderId
) {
    public static OrderPaidEvent of(Long orderId) {
        return OrderPaidEvent.builder()
                .orderId(orderId)
                .build();
    }
}
