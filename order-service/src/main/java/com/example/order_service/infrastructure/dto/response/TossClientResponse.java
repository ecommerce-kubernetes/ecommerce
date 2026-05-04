package com.example.order_service.infrastructure.dto.response;

import java.time.OffsetDateTime;

public class TossClientResponse {

    public record Confirm(
            String paymentKey,
            String orderId,
            Long totalAmount,
            String status,
            String method,
            OffsetDateTime approvedAt
    ) {
    }

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
