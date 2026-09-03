package com.example.userservice.outbox.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import com.example.userservice.outbox.exception.OutboxErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboxQueryService {

    private final OutboxRepository repository;

    public OutboxMessageResult getOutbox(Long outboxId) {
        OutboxMessage outboxMessage = repository.findById(outboxId).orElseThrow(() -> new BusinessException(OutboxErrorCode.OUTBOX_NOT_FOUND));

        return OutboxMessageResult.from(outboxMessage);
    }

    public List<OutboxMessageResult> getZombieOutboxes(LocalDateTime threshold) {
        List<OutboxMessage> outboxMessages = repository.findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus.PENDING, threshold);
        return outboxMessages.stream().map(OutboxMessageResult::from).toList();
    }

}
