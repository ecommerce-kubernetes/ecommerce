package com.example.order_service.payment.application.service.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentProvider;
import lombok.Builder;

@Builder
public record PaymentConfirmCommand(
        Long paymentId,
        Long userId,
        String paymentKey,
        Money amount,
        PaymentProvider provider
) {
}
