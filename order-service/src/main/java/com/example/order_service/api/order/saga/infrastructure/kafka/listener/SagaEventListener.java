package com.example.order_service.api.order.saga.infrastructure.kafka.listener;

import com.example.common.result.SagaProcessResult;
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
        sagaManager.processProductResult(result);
    }

    @KafkaListener(topics = "${order.topics.coupon-result}")
    public void handleCouponResult(@Payload SagaProcessResult result) {
        sagaManager.processCouponResult(result);
    }

    @KafkaListener(topics = "${order.topics.user-result}")
    public void handleUserResult(@Payload SagaProcessResult result) {
        sagaManager.processUserResult(result);
    }
}
