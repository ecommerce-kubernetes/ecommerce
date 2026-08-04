package com.example.order_service.payment.api.web.dto.request;

import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PaymentCreateRequest(
        @NotNull(message = "{payment.orderId.notNull}")
        Long orderId
) {
        public PaymentCreateCommand toCommand(Long userId) {
                return PaymentCreateCommand.builder()
                        .userId(userId)
                        .orderId(orderId)
                        .build();
        }
}
