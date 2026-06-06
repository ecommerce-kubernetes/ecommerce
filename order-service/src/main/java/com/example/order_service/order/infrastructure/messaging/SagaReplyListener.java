package com.example.order_service.order.infrastructure.messaging;

import com.example.order_service.order.application.orchestrator.OrderSagaManager;
import com.example.order_service.order.infrastructure.messaging.dto.SagaReplyMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaReplyListener {

    private final ObjectMapper objectMapper;
    private final OrderSagaManager orderSagaManager;

    @KafkaListener(topics = "${order.topics.order-saga-reply}")
    public void handleSagaReply(String payload) {
        SagaReplyMessage message = toObject(payload);
        orderSagaManager.handleReply(message);
    }

    private SagaReplyMessage toObject(String payload) {
        try {
            return objectMapper.readValue(payload, SagaReplyMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("메시지 직렬화 실패",e);
        }
    }
}
