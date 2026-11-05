package com.example.userservice.user.adapter.in.listener.router;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommand;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.application.service.PointSagaProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestorePointCommandRouter implements PointSagaCommandRouter {

    private final PointSagaProcessor processor;

    @Override
    public boolean supports(PointSagaCommand command) {
        return command == PointSagaCommand.RESTORE_POINT;
    }

    @Override
    public void execute(Long sagaId, PointSagaCommandPayload payload) {
        processor.refund(sagaId, payload.executionId(), payload.userId(), Money.wons(payload.usedPoints()));
    }

    @Override
    public void fail(Long sagaId, PointSagaCommandPayload payload, String errorCode) {
        processor.failRefund(sagaId, payload.executionId(), errorCode);
    }
}
