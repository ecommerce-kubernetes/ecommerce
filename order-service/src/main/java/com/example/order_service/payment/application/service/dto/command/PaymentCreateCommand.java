package com.example.order_service.payment.application.service.dto.command;

import lombok.Builder;

@Builder
public record PaymentCreateCommand(
        Long userId,
        Long orderId
) {
}
