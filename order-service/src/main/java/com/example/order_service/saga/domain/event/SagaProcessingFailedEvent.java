package com.example.order_service.saga.domain.event;

import lombok.Builder;

@Builder
public record SagaProcessingFailedEvent(
        Long orderId,
        String failureReason
) {
}
