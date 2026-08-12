package com.example.order_service.payment.adapter.in.web.dto.request;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.domain.PaymentProvider;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PaymentConfirmRequest(
        @NotBlank(message = "{payment.paymentKey.notBlank}")
        String paymentKey,
        @NotNull(message = "{payment.amount.notNull}")
        @Min(value = 1, message = "{payment.amount.min}")
        Long amount,
        @NotBlank(message = "{payment.provider.notBlank}")
        String provider
) {
    public PaymentConfirmCommand toCommand(Long paymentId, Long userId) {
        return PaymentConfirmCommand.builder()
                .paymentId(paymentId)
                .userId(userId)
                .paymentKey(paymentKey)
                .amount(Money.wons(amount))
                .provider(PaymentProvider.from(provider))
                .build();
    }
}
