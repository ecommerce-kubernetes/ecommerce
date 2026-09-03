package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.application.port.PointSagaReplyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PointSagaProcessor {

    private final PointCommandService pointCommandService;

    private final PointSagaReplyPort pointSagaReplyPort;

    public void deduct(Long sagaId, Long executionId, Long userId, Money amount) {
        pointCommandService.deductPoint(userId, executionId, amount);
        pointSagaReplyPort.recordForwardSuccess(sagaId, executionId);
    }

    public void refund(Long sagaId, Long executionId, Long userId, Money amount) {
        pointCommandService.addPoint(userId, executionId, amount);
        pointSagaReplyPort.recordCompensateSuccess(sagaId, executionId);
    }

    public void failDeduct(Long sagaId, Long executionId, String reason) {
        pointSagaReplyPort.recordForwardFail(sagaId, executionId, reason);
    }

    public void failRefund(Long sagaId, Long executionId, String reason) {
        pointSagaReplyPort.recordCompensateFail(sagaId, executionId, reason);
    }
}
