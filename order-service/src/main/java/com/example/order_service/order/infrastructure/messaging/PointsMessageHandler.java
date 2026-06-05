package com.example.order_service.order.infrastructure.messaging;

import com.example.order_service.order.application.messaging.SagaMessageHandler;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.infrastructure.config.SagaProperties;
import com.example.order_service.order.infrastructure.messaging.dto.PointsMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointsMessageHandler implements SagaMessageHandler {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SagaProperties sagaProperties;

    @Override
    public SagaStep supportsForward() {
        return SagaStep.POINTS_DEDUCT_PENDING;
    }

    @Override
    public SagaStep supportsCompensation() {
        return SagaStep.POINTS_RESTORE_PENDING;
    }

    @Override
    public void compensate(SagaMessage message) {
        PointsMessage restore = PointsMessage.restore(message);
        sendMessage(restore);
    }

    @Override
    public void forward(SagaMessage message) {
        PointsMessage deduct = PointsMessage.deduct(message);
        sendMessage(deduct);
    }

    private void sendMessage(PointsMessage message) {
        String topicName = sagaProperties.getPointsSagaCommand();
        String jsonPayload = toJson(message);
        kafkaTemplate.send(topicName, message.getOrderNo(), jsonPayload);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("카프카 메시지 직렬화 실패", e);
        }
    }
}
