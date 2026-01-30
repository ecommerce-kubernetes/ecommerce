package com.example.order_service.api.order.saga.orchestrator.dto.command;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SagaStepResultCommand {
    private SagaStep step;
    private String orderNo;
    private boolean isSuccess;
    private String errorCode;
    private String failureReason;

    public static SagaStepResultCommand of(SagaStep step, String orderNo, boolean isSuccess, String errorCode, String failureReason) {
        return SagaStepResultCommand.builder()
                .step(step)
                .orderNo(orderNo)
                .isSuccess(isSuccess)
                .errorCode(errorCode)
                .failureReason(failureReason)
                .build();
    }
}
