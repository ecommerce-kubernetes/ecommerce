package com.example.order_service.order.application.event;

import com.example.order_service.saga.domain.tmp.OrderSagaInstance;
import com.example.order_service.saga.domain.SagaStatus;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.saga.domain.tmp.SagaPayloadDeprecated;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Deprecated
public class OrderSagaProcessEvent {
    private Long sagaId;
    private String orderNo;
    private SagaStatus status;
    private SagaStep step;
    private SagaPayloadDeprecated payload;

    public static OrderSagaProcessEvent from(OrderSagaInstance instance) {
        return OrderSagaProcessEvent.builder()
                .sagaId(instance.getId())
                .orderNo(instance.getOrderNo())
                .status(instance.getStatus())
                .step(instance.getCurrentStep())
                .payload(instance.getPayload())
                .build();
    }
}
