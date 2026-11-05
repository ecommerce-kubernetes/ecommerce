package com.example.userservice.outbox.adapter.out.message;

import com.example.userservice.common.exception.PortException;
import com.example.userservice.outbox.application.port.OutboxMessageSendPort;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.exception.OutboxPortErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboxMessageSendAdapter implements OutboxMessageSendPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void send(OutboxMessageResult result) {
        Message<String> message = createKafkaMessage(result);
        kafkaTemplate.send(message);
    }

    private Message<String> createKafkaMessage(OutboxMessageResult result) {
        try {
            Map<String, Object> headerMap = objectMapper.readValue(
                    result.headers(),
                    new TypeReference<>() {}
            );

            return MessageBuilder
                    .withPayload(result.payload())
                    .setHeader(KafkaHeaders.TOPIC, result.topic())
                    .setHeader(KafkaHeaders.KEY, result.routingKey())
                    .copyHeaders(headerMap)
                    .build();

        } catch (JsonProcessingException e) {
            throw new PortException(OutboxPortErrorCode.MESSAGE_DESERIALIZATION_ERROR);
        }
    }
}
