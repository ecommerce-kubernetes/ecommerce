package com.example.order_service.order.infrastructure.messaging;

import com.example.order_service.order.application.messaging.SagaMessageHandler;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryMessageHandler implements SagaMessageHandler {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public SagaStep supportsForward() {
        return SagaStep.INVENTORY_DEDUCT_PENDING;
    }

    @Override
    public SagaStep supportsCompensation() {
        return SagaStep.INVENTORY_RESTORE_PENDING;
    }

    @Override
    public void compensate(SagaMessage message) {

    }

    @Override
    public void forward(SagaMessage message) {

    }
}
