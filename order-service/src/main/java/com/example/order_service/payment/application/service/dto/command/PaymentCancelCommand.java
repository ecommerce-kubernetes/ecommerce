package com.example.order_service.payment.application.service.dto.command;

import lombok.Builder;

@Builder
public record PaymentCancelCommand(
        Long orderId,
        Long userId,
        String cancelReason
) {

    public static PaymentCancelCommand of(Long orderId, Long userId, String cancelReason) {
        return PaymentCancelCommand.builder()
                .orderId(orderId)
                .userId(userId)
                .cancelReason(cancelReason)
                .build();
    }
}
