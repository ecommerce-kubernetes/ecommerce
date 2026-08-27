package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

@Builder
public record TossConfirmRequest(
        Long orderId,
        String paymentKey,
        Long amount
) {
    public static TossConfirmRequest of(Long orderId, String paymentKey, Long amount) {
        return TossConfirmRequest.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .amount(amount)
                .build();
    }
}
