package com.example.order_service.messaging;

import com.example.common.ProductStockDeductedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PRODUCT_TOPIC = "product.stock.deducted";

    @KafkaListener(topics = PRODUCT_TOPIC)
    public void productSagaListener(@Payload ProductStockDeductedEvent event){
        log.info("orderId = {}", event.getOrderId());
    }
}
