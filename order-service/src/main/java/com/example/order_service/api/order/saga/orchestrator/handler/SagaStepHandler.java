package com.example.order_service.api.order.saga.orchestrator.handler;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;

public interface SagaStepHandler {
    SagaStep getSagaStep();
    void process(Long sagaId, String orderNo, Payload payload);
    void compensate(Long sagaId, String orderNo, Payload payload);
}
