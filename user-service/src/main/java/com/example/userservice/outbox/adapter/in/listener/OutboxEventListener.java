package com.example.userservice.outbox.adapter.in.listener;

import com.example.userservice.outbox.application.service.OutboxMessagePublisher;
import com.example.userservice.outbox.application.service.dto.event.OutboxCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final OutboxMessagePublisher publisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCreatedEvent(OutboxCreatedEvent event) {
        publisher.publishMessage(event.outboxId());
    }
}
