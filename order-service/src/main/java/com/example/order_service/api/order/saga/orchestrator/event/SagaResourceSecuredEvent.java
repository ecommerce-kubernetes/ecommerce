package com.example.order_service.api.order.saga.orchestrator.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaResourceSecuredEvent {
    private Long sagaId;
    private Long orderId;
    private Long userId;

    @Builder
    private SagaResourceSecuredEvent(Long sagaId, Long orderId, Long userId) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
    }

    public static SagaResourceSecuredEvent of(Long sagaId, Long orderId, Long userId) {
        return SagaResourceSecuredEvent.builder()
                .sagaId(sagaId)
                .orderId(orderId)
                .userId(userId)
                .build();
    }
}
