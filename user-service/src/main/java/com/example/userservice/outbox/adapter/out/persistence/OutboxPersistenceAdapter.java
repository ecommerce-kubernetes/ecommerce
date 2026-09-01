package com.example.userservice.outbox.adapter.out.persistence;

import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    @Override
    public OutboxMessage save(OutboxMessage outboxMessage) {
        return outboxJpaRepository.save(outboxMessage);
    }

    @Override
    public List<OutboxMessage> findOutboxMessageByStatus(OutboxStatus status) {
        return outboxJpaRepository.findOutboxMessageByStatus(status);
    }

    @Override
    public Optional<OutboxMessage> findById(Long id) {
        return outboxJpaRepository.findById(id);
    }
}
