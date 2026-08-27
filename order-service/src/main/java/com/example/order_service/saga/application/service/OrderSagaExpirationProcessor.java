package com.example.order_service.saga.application.service;

import com.example.order_service.saga.application.service.dto.result.OrderSagaExecutionResult;
import com.example.order_service.saga.config.SagaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaExpirationProcessor {

    private final SagaProperties sagaProperties;
    private final OrderSagaQueryService orderSagaQueryService;
    private final OrderSagaCommandService orderSagaCommandService;

    public void processTimeoutForwardPendingSagas(LocalDateTime currentTime) {
        LocalDateTime timeoutThreshold = currentTime.minusMinutes(sagaProperties.timeoutForwardPending());

        List<OrderSagaExecutionResult> timeoutExecutions = orderSagaQueryService.getForwardPendingExecutionsBefore(timeoutThreshold);

        if (timeoutExecutions.isEmpty()) {
            return;
        }

        log.info("[OrderSagaExpirationProcessor] 정방향 대기 타임아웃 대상 건수: {}", timeoutExecutions.size());

        for (OrderSagaExecutionResult execution : timeoutExecutions) {
            try {
                orderSagaCommandService.failForward(execution.sagaId(), execution.executionId(), "정방향 처리 타임아웃");
            } catch (Exception e) {
                log.error("[OrderSagaExpirationProcessor] 정방향 타임아웃 실패 처리 실패 - sagaId: {}, executionId: {}",
                        execution.sagaId(), execution.executionId(), e);
            }
        }
    }

    public void processTimeoutCompensatePendingSagas(LocalDateTime currentTime) {
        LocalDateTime timeoutThreshold = currentTime.minusMinutes(sagaProperties.timeoutCompensatePending());

        List<OrderSagaExecutionResult> timeoutExecutions = orderSagaQueryService.getCompensatePendingExecutionsBefore(timeoutThreshold);

        if (timeoutExecutions.isEmpty()) {
            return;
        }

        log.info("[OrderSagaExpirationProcessor] 보상 대기 타임아웃 대상 건수: {}", timeoutExecutions.size());

        for (OrderSagaExecutionResult execution : timeoutExecutions) {
            try {
                orderSagaCommandService.retryCompensate(execution.sagaId(), execution.executionId());
            } catch (Exception e) {
                log.error("[OrderSagaExpirationProcessor] 보상 대기 이벤트 재발행 실패 - sagaId: {}, executionId: {}",
                        execution.sagaId(), execution.executionId(), e);
            }
        }
    }
}
