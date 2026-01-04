package com.example.order_service.api.order.saga.orchestrator.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaResourceSecuredEvent {
    private Long sagaId;
    private String orderNo;
    private Long userId;

    @Builder
    private SagaResourceSecuredEvent(Long sagaId, String orderNo, Long userId) {
        this.sagaId = sagaId;
        this.orderNo = orderNo;
        this.userId = userId;
    }

    public static SagaResourceSecuredEvent of(Long sagaId, String orderNo, Long userId) {
        return SagaResourceSecuredEvent.builder()
                .sagaId(sagaId)
                .orderNo(orderNo)
                .userId(userId)
                .build();
    }
}
