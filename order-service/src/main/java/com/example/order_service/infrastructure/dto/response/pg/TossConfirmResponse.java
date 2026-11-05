package com.example.order_service.infrastructure.dto.response.pg;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record TossConfirmResponse(
        String status,
        Long totalAmount,
        String method,
        String lastTransactionKey,
        OffsetDateTime approvedAt
) {
}
