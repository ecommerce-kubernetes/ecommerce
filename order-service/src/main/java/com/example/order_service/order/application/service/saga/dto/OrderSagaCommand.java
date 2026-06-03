package com.example.order_service.order.application.service.saga.dto;

import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;

public class OrderSagaCommand {

    @Builder
    public record Create(
            String orderNo,
            SagaStep step,
            SagaStatus status,
            SagaPayload payload
    ) {
        public static Create of(String orderNo, SagaStep step, SagaStatus status, SagaPayload payload) {
            return Create.builder()
                    .orderNo(orderNo)
                    .step(step)
                    .status(status)
                    .payload(payload)
                    .build();
        }
    }
}
