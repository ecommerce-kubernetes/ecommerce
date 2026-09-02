package com.example.userservice.outbox.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.outbox.exception.OutboxErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxCommandService {
    private final IdGenerator idGenerator;

    private final OutboxRepository outboxRepository;

    public void createOutbox(CreateOutboxMessageContext context) {
        OutboxMessage outboxMessage = OutboxMessage.create(context, idGenerator);

        outboxRepository.save(outboxMessage);
    }

    public void changeSent(Long id) {
        OutboxMessage outboxMessage = outboxRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OutboxErrorCode.OUT_BOX_NOT_FOUND));

        outboxMessage.sent();
    }
}
