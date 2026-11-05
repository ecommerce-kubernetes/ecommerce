package com.example.userservice.outbox.application.port;

import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxRepository {

    OutboxMessage save(OutboxMessage outboxMessage);

    List<OutboxMessage> findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime threshold);

    Optional<OutboxMessage> findById(Long id);
}
