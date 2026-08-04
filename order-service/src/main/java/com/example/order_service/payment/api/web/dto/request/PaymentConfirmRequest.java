package com.example.order_service.payment.api.web.dto.request;

import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PaymentConfirmRequest(
        @NotNull(message = "{payment.orderId.notNull}")
        Long orderId,
        @NotBlank(message = "{payment.paymentKey.notBlank}")
        String paymentKey,
        @NotNull(message = "{payment.amount.notNull}")
        @Min(value = 1, message = "{payment.amount.min}")
        Long amount
) {
    public static PaymentCommand.Confirm toCommand(Long userId) {
        return null;
    }
}
