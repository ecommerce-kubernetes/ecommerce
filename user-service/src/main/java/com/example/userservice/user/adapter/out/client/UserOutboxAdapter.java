package com.example.userservice.user.adapter.out.client;

import com.example.userservice.outbox.application.service.OutboxCommandService;
import com.example.userservice.user.application.port.UserOutboxPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOutboxAdapter implements UserOutboxPort {

    private final OutboxCommandService outboxCommandService;
    private final ObjectMapper objectMapper;

    @Override
    public void recordForwardSuccess(Long sagaId, Long executionId) {

    }

    @Override
    public void recordCompensateSuccess(Long sagaId, Long executionId) {

    }
}
