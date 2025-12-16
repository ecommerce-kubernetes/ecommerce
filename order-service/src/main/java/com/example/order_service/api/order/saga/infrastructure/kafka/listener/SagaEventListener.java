package com.example.order_service.api.order.saga.infrastructure.kafka.listener;

import com.example.common.SagaProcessResult;
import com.example.common.SagaStatus;
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
        if (result.getStatus() == SagaStatus.SUCCESS){
            sagaManager.proceedToCoupon(result.getSagaId());
        } else if (result.getStatus() == SagaStatus.FAIL) {
            sagaManager.abortSaga(result.getSagaId(), result.getFailureReason());
        }
    }
}
