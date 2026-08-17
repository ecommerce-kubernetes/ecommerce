package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.adapter.out.message.processor.dto.CouponMessagePayload;
import com.example.order_service.saga.adapter.out.message.processor.dto.InventoryMessagePayload;
import com.example.order_service.saga.config.SagaTopicProperties;
import com.example.order_service.saga.domain.event.*;
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
public class CouponMessageProcessor implements SagaMessageProcessor {

    private final ObjectMapper objectMapper;
    private final SagaTopicProperties topic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public boolean supports(SagaEvent event) {
        return event instanceof UsedCouponEvent ||
                event instanceof RestoreCouponEvent;
    }

    @Override
    public void process(SagaEvent event) {
        if (event instanceof UsedCouponEvent usedEvent) {
            CouponMessagePayload payload = CouponMessagePayload.from(usedEvent);
            sendKafkaMessage(event, payload, "UsedCouponCommand");

        } else if (event instanceof RestoreCouponEvent restoreEvent) {
            CouponMessagePayload payload = CouponMessagePayload.from(restoreEvent);
            sendKafkaMessage(event, payload, "RestoreCouponCommand");
        }
    }

    private void sendKafkaMessage(SagaEvent event, CouponMessagePayload payload, String eventType) {
        String jsonPayload = toJson(payload);
        Message<String> kafkaMessage = createKafkaMessage(event, jsonPayload, eventType);
        kafkaTemplate.send(kafkaMessage);
    }

    private String toJson(CouponMessagePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new MessageSerializationException("쿠폰 사가 메시지 직렬화 실패.");
        }
    }

    private Message<String> createKafkaMessage(SagaEvent event, String payload, String eventType) {
        return MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic.couponSagaCommand())
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.orderId()))
                .setHeader("X-Saga-Id", event.sagaId())
                .setHeader("X-Event-Type", eventType)
                .build();
    }
}
