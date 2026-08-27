package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.common.exception.PortException;
import com.example.order_service.saga.adapter.out.message.processor.dto.CouponMessagePayload;
import com.example.order_service.saga.adapter.out.message.processor.dto.SagaCommandType;
import com.example.order_service.saga.config.SagaTopicProperties;
import com.example.order_service.saga.domain.event.RestoreCouponEvent;
import com.example.order_service.saga.domain.event.SagaEvent;
import com.example.order_service.saga.domain.event.UsedCouponEvent;
import com.example.order_service.saga.exception.SagaErrorCode;
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
            sendKafkaMessage(event, payload, SagaCommandType.USE_COUPON);

        } else if (event instanceof RestoreCouponEvent restoreEvent) {
            CouponMessagePayload payload = CouponMessagePayload.from(restoreEvent);
            sendKafkaMessage(event, payload, SagaCommandType.RESTORE_COUPON);
        }
    }

    private void sendKafkaMessage(SagaEvent event, CouponMessagePayload payload, SagaCommandType eventType) {
        String jsonPayload = toJson(payload);
        Message<String> kafkaMessage = createKafkaMessage(event, jsonPayload, eventType);
        kafkaTemplate.send(kafkaMessage);
    }

    private String toJson(CouponMessagePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new PortException(SagaErrorCode.SAGA_MESSAGE_SERIALIZATION_FAILED);
        }
    }

    private Message<String> createKafkaMessage(SagaEvent event, String payload, SagaCommandType commandType) {
        return MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic.couponSagaCommand())
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.orderId()))
                .setHeader("X-Saga-Id", event.sagaId())
                .setHeader("X-Command-Type", commandType.name())
                .build();
    }
}
