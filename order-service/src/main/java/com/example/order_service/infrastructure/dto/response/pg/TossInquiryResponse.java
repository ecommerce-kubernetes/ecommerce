package com.example.order_service.infrastructure.dto.response.pg;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

public record TossInquiryResponse(
        String lastTransactionKey,
        String status,
        FailureResponse failure,
        List<CancelReceipt> cancels

) {
    @Builder
    public record FailureResponse(
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
}
