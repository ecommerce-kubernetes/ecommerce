package com.example.order_service.api.order.saga.orchestrator.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SagaAbortEvent {
    private Long sagaId;
    private String orderNo;
    private Long userId;
    private String failureCode;


    public static SagaAbortEvent of(Long sagaId, String orderNo, Long userId, String failureCode){
        return SagaAbortEvent.builder()
                .sagaId(sagaId)
                .orderNo(orderNo)
                .userId(userId)
                .failureCode(failureCode)
                .build();
    }
}
