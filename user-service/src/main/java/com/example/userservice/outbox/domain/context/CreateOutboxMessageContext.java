package com.example.userservice.outbox.domain.context;

import lombok.Builder;

@Builder
public record CreateOutboxMessageContext(String topic, String routingKey, String headers, String payload) {
}
