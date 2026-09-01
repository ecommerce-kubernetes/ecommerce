package com.example.userservice.outbox.application.service.dto;

import com.example.userservice.outbox.domain.OutboxMessage;
import lombok.Builder;

@Builder
public record OutboxMessageResult(Long id, String topic, String routingKey, String headers, String payload) {

    public static OutboxMessageResult from(OutboxMessage message) {
        return OutboxMessageResult.builder()
                .id(message.getId())
                .topic(message.getTopic())
                .routingKey(message.getRoutingKey())
                .headers(message.getHeaders())
                .payload(message.getPayload())
                .build();
    }
}
