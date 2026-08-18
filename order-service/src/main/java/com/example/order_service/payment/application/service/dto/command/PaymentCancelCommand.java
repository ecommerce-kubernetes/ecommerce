package com.example.order_service.payment.application.service.dto.command;

import lombok.Builder;

@Builder
public record PaymentCancelCommand(
        Long orderId,
        Long userId
) {

    public static PaymentCancelCommand of(Long orderId, Long userId) {
        return PaymentCancelCommand.builder()
                .orderId(orderId)
                .userId(userId)
                .build();
    }
}
