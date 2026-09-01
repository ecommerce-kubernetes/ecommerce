package com.example.userservice.outbox.application.service;

import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboxQueryService {

    private final OutboxRepository repository;

    public List<OutboxMessageResult> getPendingOutbox() {
        List<OutboxMessage> outboxMessages = repository.findOutboxMessageByStatus(OutboxStatus.PENDING);
        return outboxMessages.stream().map(OutboxMessageResult::from).toList();
    }

}
