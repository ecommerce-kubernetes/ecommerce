package com.example.userservice.outbox.application.port;

import com.example.userservice.outbox.domain.OutboxMessage;

public interface OutboxRepository {

    OutboxMessage save(OutboxMessage outboxMessage);
}
