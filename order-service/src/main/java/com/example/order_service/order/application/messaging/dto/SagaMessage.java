package com.example.order_service.order.application.messaging.dto;

import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SagaMessage {
    private Long sagaId;
    private String orderNo;
    private SagaStatus status;
    private SagaStep step;
    private SagaPayload payload;

    public static SagaMessage from(OrderSagaProcessEvent event){
        return SagaMessage.builder()
                .sagaId(event.getSagaId())
                .orderNo(event.getOrderNo())
                .status(event.getStatus())
                .step(event.getStep())
                .payload(event.getPayload())
                .build();
    }
}
