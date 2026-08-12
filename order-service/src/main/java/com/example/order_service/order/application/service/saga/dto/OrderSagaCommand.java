package com.example.order_service.order.application.service.saga.dto;

import com.example.order_service.saga.domain.tmp.SagaStep;
import com.example.order_service.saga.domain.tmp.StepResult;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;

public class OrderSagaCommand {

    @Builder
    public record Create(
            String orderNo,
            Long paymentId,
            SagaStep step,
            SagaPayload payload
    ) {
        public static Create of(String orderNo, Long paymentId, SagaStep step, SagaPayload payload) {
            return Create.builder()
                    .orderNo(orderNo)
                    .paymentId(paymentId)
                    .step(step)
                    .payload(payload)
                    .build();
        }
    }

    @Builder
    public record RecordHistory(
            String orderNo,
            StepResult status,
            SagaStep step,
            String code
    ) {
        public static RecordHistory of(String orderNo, StepResult status, SagaStep step, String code) {
            return RecordHistory.builder()
                    .orderNo(orderNo)
                    .status(status)
                    .step(step)
                    .code(code)
                    .build();
        }
    }
}
