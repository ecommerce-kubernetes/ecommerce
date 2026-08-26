package com.example.order_service.payment.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PGInquiryResult(
        String transactionKey,
        PaymentPGStatus status,
        PGFailureResult failure,
        Money cancelAmount,
        String cancelReason,
        LocalDateTime canceledAt
) {

    @Builder
    public record PGFailureResult(
            String code,
            String message
    ) {
    }
}
