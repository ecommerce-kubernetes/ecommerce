package com.example.product_service.api.product.saga.listener;

import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.product.saga.service.SagaProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final SagaProcessor sagaProcessor;

    @KafkaListener(topics = "${product.topics.product-saga-command}")
    public void handleOrderEvent(@Payload ProductSagaCommand event) {
        sagaProcessor.productSagaProcess(event);
    }
}
