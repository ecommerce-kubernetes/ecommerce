package com.example.order_service.saga.domain.event;

import lombok.Builder;

@Builder
public record SagaSuccessEvent(
        Long orderId
) {
}
