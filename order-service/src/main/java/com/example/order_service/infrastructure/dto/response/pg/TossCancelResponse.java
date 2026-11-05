package com.example.order_service.infrastructure.dto.response.pg;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record TossCancelResponse(
        String status,
        List<CancelReceipt> cancels
) {

    @Builder
    public record CancelReceipt(
        String transactionKey,
        Long cancelAmount,
        OffsetDateTime canceledAt,
        String cancelReason
    ) {
    }
}
