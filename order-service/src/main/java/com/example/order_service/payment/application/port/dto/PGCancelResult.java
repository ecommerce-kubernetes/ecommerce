package com.example.order_service.payment.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PGCancelResult(
        PaymentPGStatus status,
        String transactionKey,
        Money amount,
        String cancelReason,
        LocalDateTime canceledAt
) {
}
