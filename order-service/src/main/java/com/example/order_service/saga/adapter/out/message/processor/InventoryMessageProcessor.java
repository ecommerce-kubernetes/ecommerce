package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.adapter.out.message.processor.dto.InventoryMessagePayload;
import com.example.order_service.saga.config.SagaTopicProperties;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.saga.domain.event.RestoreInventoryEvent;
import com.example.order_service.saga.domain.event.SagaEvent;
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
public class InventoryMessageProcessor implements SagaMessageProcessor {

    private final ObjectMapper objectMapper;
    private final SagaTopicProperties topic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public boolean supports(SagaEvent event) {
        return event instanceof ReduceInventoryEvent ||
                event instanceof RestoreInventoryEvent;
    }

    @Override
    public void process(SagaEvent event) {
        if (event instanceof ReduceInventoryEvent reduceEvent) {
            InventoryMessagePayload payload = InventoryMessagePayload.from(reduceEvent);
            sendKafkaMessage(event, payload, "ReduceInventoryCommand");

        } else if (event instanceof RestoreInventoryEvent restoreEvent) {
            InventoryMessagePayload payload = InventoryMessagePayload.from(restoreEvent);
            sendKafkaMessage(event, payload, "RestoreInventoryCommand");
        }
    }

    private void sendKafkaMessage(SagaEvent event, InventoryMessagePayload payload, String eventType) {
        String jsonPayload = toJson(payload);
        Message<String> kafkaMessage = createKafkaMessage(event, jsonPayload, eventType);
        kafkaTemplate.send(kafkaMessage);
    }

    private String toJson(InventoryMessagePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new MessageSerializationException("재고 사가 메시지 직렬화 실패.");
        }
    }

    private Message<String> createKafkaMessage(SagaEvent event, String payload, String eventType) {
        return MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic.productSagaCommand())
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.orderId()))
                .setHeader("X-Saga-Id", event.sagaId())
                .setHeader("X-Event-Type", eventType)
                .build();
    }
}
