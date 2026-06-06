package com.example.order_service.order.application.service.saga.dto;

import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;
import lombok.Getter;

public class OrderSagaResult {

    @Builder
    public record Default(
            Long sagaId,
            String orderNo,
            SagaStep currentStep,
            SagaStatus status,
            SagaPayload payload
    ) {
        public static Default from(OrderSagaInstance instance) {
            return Default.builder()
                    .sagaId(instance.getId())
                    .orderNo(instance.getOrderNo())
                    .currentStep(instance.getCurrentStep())
                    .status(instance.getStatus())
                    .payload(instance.getPayload())
                    .build();
        }
    }
}
