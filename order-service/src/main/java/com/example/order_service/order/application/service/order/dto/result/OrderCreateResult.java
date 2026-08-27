package com.example.order_service.order.application.service.order.dto.result;

import lombok.Builder;

@Builder
public record OrderCreateResult(
        Long orderId
) {
    public static OrderCreateResult of(Long orderId) {
        return OrderCreateResult.builder()
                .orderId(orderId)
                .build();
    }
}
