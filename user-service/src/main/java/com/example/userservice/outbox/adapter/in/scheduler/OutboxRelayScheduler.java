package com.example.userservice.outbox.adapter.in.scheduler;

import com.example.userservice.outbox.application.service.OutboxMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {
    private final OutboxMessagePublisher publisher;
}
