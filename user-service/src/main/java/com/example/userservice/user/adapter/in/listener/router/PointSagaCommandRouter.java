package com.example.userservice.user.adapter.in.listener.router;

import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommand;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;

public interface PointSagaCommandRouter {

    boolean supports(PointSagaCommand command);

    void execute(Long sagaId, PointSagaCommandPayload payload);

    void fail(Long sagaId, PointSagaCommandPayload payload, String errorCode);
}
