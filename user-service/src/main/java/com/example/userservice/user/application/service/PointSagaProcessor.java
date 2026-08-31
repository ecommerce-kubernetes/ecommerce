package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.application.port.UserOutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PointSagaProcessor {

    private final PointCommandService pointCommandService;

    private final UserOutboxPort userOutboxPort;

    public void deduct(Long sagaId, Long executionId, Long userId, Money amount) {
        pointCommandService.deductPoint(userId, executionId, amount);
        userOutboxPort.recordForwardSuccess(sagaId, executionId);
    }

    public void refund(Long sagaId, Long executionId, Long userId, Money amount) {
        pointCommandService.addPoint(userId, executionId, amount);
        userOutboxPort.recordCompensateSuccess(sagaId, executionId);
    }
}
