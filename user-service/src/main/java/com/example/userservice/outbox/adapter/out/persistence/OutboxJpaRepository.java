package com.example.userservice.outbox.adapter.out.persistence;

import com.example.userservice.outbox.domain.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxJpaRepository extends JpaRepository<OutboxMessage, Long> {
}
