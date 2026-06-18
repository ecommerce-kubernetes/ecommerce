package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

public class TossClientResponse {

    @Builder
    public record Confirm(
            String status,
            Long totalAmount,
            String method,
            String lastTransactionKey,
            OffsetDateTime approvedAt
    ) {
    }

    @Builder
    public record Cancel(
            String status,
            List<CancelReceipt> cancels
    ) {
    }

    @Builder
    public record CancelReceipt(
            String transactionKey,
            Long cancelAmount,
            OffsetDateTime canceledAt,
            String cancelReason
    ) {}

    public record Error(
            String code,
            String message
    ) {
    }
}
