package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStep;

public interface SagaMessageHandler {
    SagaStep supportsForward();
    SagaStep supportsCompensation();

    void compensate(SagaMessage message);
    void forward(SagaMessage message);
}
