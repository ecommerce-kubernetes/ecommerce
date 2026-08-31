package com.example.userservice.outbox.adapter.out.persistence;

import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.domain.OutboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    @Override
    public OutboxMessage save(OutboxMessage outboxMessage) {
        return outboxJpaRepository.save(outboxMessage);
    }
}
