package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.outbox.application.service.OutboxCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PointSagaProcessor {

    private final PointCommandService pointCommandService;

    private final OutboxCommandService outboxCommandService;

    public void deduct(Long sagaId, Long executionId, Long userId, Money amount) {

    }

    public void refund(Long sagaId, Long executionId, Long userId, Money amount) {

    }
}
