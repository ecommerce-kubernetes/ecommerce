package com.example.order_service.api.order.saga.orchestrator.event;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaAbortEvent {
    private Long sagaId;
    private String orderNo;
    private Long userId;
    private OrderFailureCode orderFailureCode;

    @Builder
    private SagaAbortEvent(Long sagaId, String orderNo, Long userId, OrderFailureCode orderFailureCode) {
        this.sagaId = sagaId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.orderFailureCode = orderFailureCode;
    }

    public static SagaAbortEvent of(Long sagaId, String orderNo, Long userId, OrderFailureCode failureCode){
        return SagaAbortEvent.builder()
                .sagaId(sagaId)
                .orderNo(orderNo)
                .userId(userId)
                .orderFailureCode(failureCode)
                .build();
    }
}
