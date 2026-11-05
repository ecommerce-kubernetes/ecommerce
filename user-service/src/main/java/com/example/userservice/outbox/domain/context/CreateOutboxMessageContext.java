package com.example.userservice.outbox.domain.context;

import lombok.Builder;

@Builder(toBuilder = true)
public record CreateOutboxMessageContext(Long id, String topic, String routingKey, String headers, String payload) {
}
