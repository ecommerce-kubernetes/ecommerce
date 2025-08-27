package com.example.product_service.messaging;

import com.example.common.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductListener {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_CREATED = "order.created";

    @KafkaListener(topics = ORDER_CREATED)
    public void inventoryReductionListener(@Payload OrderCreatedEvent event){
        log.info("orderId = {}",event.getOrderId());
    }
}
