package com.example.order_service.api.order.saga.infrastructure.kafka.listener;

import com.example.common.SagaProcessResult;
import com.example.common.SagaEventStatus;
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

    @KafkaListener(topics = "${order.topics.product-result}")
    public void handleProductResult(@Payload SagaProcessResult result){
        if (result.getStatus() == SagaEventStatus.SUCCESS){
            sagaManager.proceedSaga(result.getSagaId());
        } else if (result.getStatus() == SagaEventStatus.FAIL) {
            sagaManager.abortSaga(result.getSagaId(), result.getErrorCode(), result.getFailureReason());
        }
    }

    @KafkaListener(topics = "${order.topics.coupon-result}")
    public void handleCouponResult(@Payload SagaProcessResult result) {
        if (result.getStatus() == SagaEventStatus.SUCCESS) {
            sagaManager.proceedSaga(result.getSagaId());
        } else if (result.getStatus() == SagaEventStatus.FAIL) {
            sagaManager.abortSaga(result.getSagaId(), result.getErrorCode(), result.getFailureReason());
        }
    }

    @KafkaListener(topics = "${order.topics.user-result}")
    public void handleUserResult(@Payload SagaProcessResult result) {
        if (result.getStatus() == SagaEventStatus.SUCCESS) {
            sagaManager.proceedSaga(result.getSagaId());
        } else if (result.getStatus() == SagaEventStatus.FAIL) {
            sagaManager.abortSaga(result.getSagaId(), result.getErrorCode(), result.getFailureReason());
        }
    }
}
