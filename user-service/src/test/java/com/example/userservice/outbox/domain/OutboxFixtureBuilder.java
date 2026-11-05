package com.example.userservice.outbox.domain;

import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;

import java.util.concurrent.atomic.AtomicLong;

public class OutboxFixtureBuilder {
    private static AtomicLong idSeq = new AtomicLong(100L);
    private static final IdGenerator ID_GENERATOR = idSeq::getAndIncrement;

    private String topic = "order.saga.reply";
    private String routingKey = "1";
    private String headers = "{\"X-Reply-Type\": \"FORWARD\"}";
    private String payload = "{\"executionId\": \"1\", \"result\": \"SUCCESS\"}";
    private OutboxStatus status = OutboxStatus.PENDING;

    public static OutboxFixtureBuilder given() {
        return new OutboxFixtureBuilder();
    }

    public OutboxMessage build() {
        CreateOutboxMessageContext context = CreateOutboxMessageContext.builder()
                .id(ID_GENERATOR.generate())
                .topic(topic)
                .routingKey(routingKey)
                .headers(headers)
                .payload(payload)
                .build();

        return OutboxMessage.create(context);
    }
}
