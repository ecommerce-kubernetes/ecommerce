package com.example.order_service.api.order.saga.orchestrator.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaCompletedEvent {
    private Long sagaId;
    private Long orderId;
    private Long userId;

    @Builder
    private SagaCompletedEvent(Long sagaId, Long orderId, Long userId) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
    }

    public static SagaCompletedEvent of(Long sagaId, Long orderId, Long userId) {
        return SagaCompletedEvent.builder()
                .sagaId(sagaId)
                .orderId(orderId)
                .userId(userId)
                .build();
    }
}
