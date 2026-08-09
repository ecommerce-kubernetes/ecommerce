package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Deprecated
public class TossClientResponse {

    @Builder
    public record Inquiry(
            String paymentKey,
            String orderId,
            String status,
            Long totalAmount,
            Long balanceAmount,
            String method,
            String lastTransactionKey,
            OffsetDateTime approvedAt,
            Failure failure,
            List<CancelReceipt> cancels
    ) {
    }

    @Builder
    public record Failure(
            String code,
            String message
    ) {
    }

    @Builder
    public record CancelReceipt(
            String transactionKey,
            Long cancelAmount,
            OffsetDateTime canceledAt,
            String cancelReason
    ) {
    }

    @Builder
    public record Error(
            String code,
            String message
    ) {
    }
}
