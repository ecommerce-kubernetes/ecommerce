package com.example.userservice.outbox.adapter.out.persistence;

import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime threshold);
}
