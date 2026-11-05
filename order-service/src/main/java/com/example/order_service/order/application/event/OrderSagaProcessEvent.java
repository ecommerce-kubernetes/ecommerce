package com.example.order_service.order.application.event;

import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSagaProcessEvent {
    private Long sagaId;
    private String orderNo;
    private SagaStatus status;
    private SagaStep step;
    private SagaPayload payload;

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
