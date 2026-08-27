package com.example.order_service.saga.application.service.dto.result;

import com.example.order_service.saga.domain.OrderSagaExecution;
import lombok.Builder;

@Builder
public record OrderSagaExecutionResult(
        Long sagaId,
        Long executionId
) {
    public static OrderSagaExecutionResult from(OrderSagaExecution execution) {
        return OrderSagaExecutionResult.builder()
                .sagaId(execution.getOrderSaga().getId())
                .executionId(execution.getId())
                .build();
    }
}
