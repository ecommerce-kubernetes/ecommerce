package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.adapter.out.message.processor.dto.InventoryMessagePayload;
import com.example.order_service.saga.adapter.out.message.processor.dto.PointMessagePayload;
import com.example.order_service.saga.adapter.out.message.processor.dto.SagaCommandType;
import com.example.order_service.saga.config.SagaTopicProperties;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.saga.domain.event.SagaEvent;
import com.example.order_service.saga.domain.event.UsedPointEvent;
import com.example.order_service.saga.exception.MessageSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointMessageProcessor implements SagaMessageProcessor{

    private final ObjectMapper objectMapper;
    private final SagaTopicProperties topic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public boolean supports(SagaEvent event) {
        return event instanceof UsedPointEvent;
    }

    @Override
    public void process(SagaEvent event) {
        if (event instanceof UsedPointEvent reduceEvent) {
            PointMessagePayload payload = PointMessagePayload.from(reduceEvent);
            sendKafkaMessage(event, payload, SagaCommandType.USE_POINT);
        }
    }

    private void sendKafkaMessage(SagaEvent event, PointMessagePayload payload, SagaCommandType commandType) {
        String jsonPayload = toJson(payload);
        Message<String> kafkaMessage = createKafkaMessage(event, jsonPayload, commandType);
        kafkaTemplate.send(kafkaMessage);
    }

    private String toJson(PointMessagePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new MessageSerializationException("포인트 사가 메시지 직렬화 실패.");
        }
    }

    private Message<String> createKafkaMessage(SagaEvent event, String payload, SagaCommandType commandType) {
        return MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic.productSagaCommand())
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.orderId()))
                .setHeader("X-Saga-Id", event.sagaId())
                .setHeader("X-Command-Type", commandType.name())
                .build();
    }
}
