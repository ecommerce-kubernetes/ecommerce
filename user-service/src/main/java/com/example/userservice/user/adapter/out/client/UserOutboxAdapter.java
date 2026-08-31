package com.example.userservice.user.adapter.out.client;

import com.example.userservice.outbox.application.service.OutboxCommandService;
import com.example.userservice.user.application.port.UserOutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOutboxAdapter implements UserOutboxPort {

    private final OutboxCommandService outboxCommandService;

    @Override
    public void recordPointDeduct(Long sagaId, Long executionId) {

    }

    @Override
    public void recordPointRefund(Long sagaId, Long executionId) {

    }
}
