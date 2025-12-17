package com.example.order_service.api.order.saga.orchestrator.event;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaAbortEvent {
    private Long sagaId;
    private Long orderId;
    private Long userId;
    private OrderFailureCode orderFailureCode;

    @Builder
    private SagaAbortEvent(Long sagaId, Long orderId, Long userId, OrderFailureCode orderFailureCode) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.orderFailureCode = orderFailureCode;
    }

    public static SagaAbortEvent of(Long sagaId, Long orderId, Long userId, OrderFailureCode failureCode){
        return SagaAbortEvent.builder()
                .sagaId(sagaId)
                .orderId(orderId)
                .userId(userId)
                .orderFailureCode(failureCode)
                .build();
    }
}
