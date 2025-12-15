package com.example.order_service.api.order.saga.infrastructure;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestInventoryDeduction(Long sagaId, Payload payload) {
        kafkaTemplate.send("order.saga.inventory.deduct", String.valueOf(sagaId), payload.getSagaItems());
    }
}
