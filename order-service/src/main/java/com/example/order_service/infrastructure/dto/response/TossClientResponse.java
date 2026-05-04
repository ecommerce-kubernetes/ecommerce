package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;

public class TossClientResponse {

    @Builder
    public record Confirm(
            String paymentKey,
            String orderId,
            Long totalAmount,
            String status,
            String method,
            OffsetDateTime approvedAt
    ) {
    }

    @Builder
    public record Cancel(
            String paymentKey,
            String orderId,
            Long totalAmount,
            String status,
            String method,
            OffsetDateTime approvedAt
    ) {
    }

    public record Error(
            String code,
            String message
    ) {
    }
}
