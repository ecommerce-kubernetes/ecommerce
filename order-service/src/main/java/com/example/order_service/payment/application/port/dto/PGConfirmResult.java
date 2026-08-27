package com.example.order_service.payment.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentMethod;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PGConfirmResult(
        PaymentPGStatus status,
        Money amount,
        PaymentMethod method,
        String transactionKey,
        LocalDateTime approvedAt
) {
}
