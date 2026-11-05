package com.example.order_service.saga.adapter.in.listener.dto;

import lombok.Builder;

@Builder
public record SagaReplyMessagePayload(
        Long executionId,
        SagaReplyResult result,
        String failureReason
) {
}
