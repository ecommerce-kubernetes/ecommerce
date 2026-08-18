package com.example.order_service.saga.domain.event;

import lombok.Builder;

@Builder
public record SagaFailedEvent(
        Long orderId,
        String failureReason
) {
}
