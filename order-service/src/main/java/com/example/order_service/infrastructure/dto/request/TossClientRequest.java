package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

public class TossClientRequest {

    @Builder
    public record Confirm(
            String orderId,
            String paymentKey,
            Long amount
    ) {
    }

    @Builder
    public record Cancel(
            String cancelReason,
            Long cancelAmount
    ) {
    }

}
