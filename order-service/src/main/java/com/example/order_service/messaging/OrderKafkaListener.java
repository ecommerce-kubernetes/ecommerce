package com.example.order_service.messaging;

import com.example.common.FailedEvent;
import com.example.common.SuccessSagaEvent;
import com.example.order_service.service.SagaManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaListener {
    private final SagaManager sagaManager;
    private static final String PRODUCT_SUCCESS_TOPIC = "product.stock.deducted";
    private static final String COUPON_SUCCESS_TOPIC = "coupon.used.applied";
    private static final String USER_SUCCESS_TOPIC = "user.cash.deducted";
    private static final String USER_FAILURE_TOPIC = "user.cash.failed";
    private static final String COUPON_FAILURE_TOPIC = "coupon.used.failed";
    private static final String PRODUCT_FAILURE_TOPIC = "product.stock.failed";

    @KafkaListener(topics = {PRODUCT_SUCCESS_TOPIC, COUPON_SUCCESS_TOPIC, USER_SUCCESS_TOPIC})
    public void sagaSuccessListener(@Payload SuccessSagaEvent event){
        log.info("receive message = {}", event.getClass());
        sagaManager.processSagaSuccess(event);
    }

    @KafkaListener(topics = {USER_FAILURE_TOPIC, COUPON_FAILURE_TOPIC, PRODUCT_FAILURE_TOPIC})
    public void sagaFailureListener(@Payload FailedEvent event){
        log.info("received message = {}", event.getClass());
        sagaManager.processSagaFailure(event);
    }
}
