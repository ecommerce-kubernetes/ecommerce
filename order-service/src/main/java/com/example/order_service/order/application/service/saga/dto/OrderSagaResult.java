package com.example.order_service.order.application.service.saga.dto;

import com.example.order_service.saga.domain.tmp.OrderSagaInstance;
import com.example.order_service.saga.domain.SagaStatus;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.saga.domain.tmp.SagaPayloadDeprecated;
import lombok.Builder;

public class OrderSagaResult {

    @Builder
    public record Default(
            Long sagaId,
            String orderNo,
            SagaStep currentStep,
            SagaStatus status,
            SagaPayloadDeprecated payload,
            String causeCode
    ) {
        public static Default from(OrderSagaInstance instance) {
            return Default.builder()
                    .sagaId(instance.getId())
                    .orderNo(instance.getOrderNo())
                    .currentStep(instance.getCurrentStep())
                    .status(instance.getStatus())
                    .payload(instance.getPayload())
                    .causeCode(instance.getCauseCode())
                    .build();
        }
    }
}
