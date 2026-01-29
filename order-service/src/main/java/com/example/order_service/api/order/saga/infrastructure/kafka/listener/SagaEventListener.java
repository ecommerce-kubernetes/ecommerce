package com.example.order_service.api.order.saga.infrastructure.kafka.listener;

import com.example.common.result.SagaEventStatus;
import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStepResultCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final SagaManager sagaManager;

    @KafkaListener(topics = "${order.topics.product-saga-reply}")
    public void handleProductResult(@Payload SagaProcessResult result){
        SagaStepResultCommand command = mapToSagaStepResultCommand(SagaStep.PRODUCT, result);
        sagaManager.handleStepResult(command);
    }

    @KafkaListener(topics = "${order.topics.coupon-saga-reply}")
    public void handleCouponResult(@Payload SagaProcessResult result) {
        SagaStepResultCommand command = mapToSagaStepResultCommand(SagaStep.COUPON, result);
        sagaManager.handleStepResult(command);
    }

    @KafkaListener(topics = "${order.topics.user-saga-reply}")
    public void handleUserResult(@Payload SagaProcessResult result) {
        SagaStepResultCommand command = mapToSagaStepResultCommand(SagaStep.USER, result);
        sagaManager.handleStepResult(command);
    }

    private SagaStepResultCommand mapToSagaStepResultCommand(SagaStep step, SagaProcessResult result) {
        return SagaStepResultCommand.of(step, result.getOrderNo(), isSuccess(result.getStatus()),
                result.getErrorCode(), result.getFailureReason());
    }

    private boolean isSuccess(SagaEventStatus status) {
        return status == SagaEventStatus.SUCCESS;
    }
}
