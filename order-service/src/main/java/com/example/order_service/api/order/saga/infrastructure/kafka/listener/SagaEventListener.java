package com.example.order_service.api.order.saga.infrastructure.kafka.listener;

import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
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
        sagaManager.handleStepResult(SagaStep.PRODUCT, result);
    }

    @KafkaListener(topics = "${order.topics.coupon-saga-reply}")
    public void handleCouponResult(@Payload SagaProcessResult result) {
        sagaManager.handleStepResult(SagaStep.COUPON, result);
    }

    @KafkaListener(topics = "${order.topics.user-saga-reply}")
    public void handleUserResult(@Payload SagaProcessResult result) {
        sagaManager.handleStepResult(SagaStep.USER, result);
    }
}
