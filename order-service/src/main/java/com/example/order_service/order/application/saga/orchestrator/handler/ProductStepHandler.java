package com.example.order_service.order.application.saga.orchestrator.handler;

import com.example.order_service.order.application.saga.domain.model.SagaStep;
import com.example.order_service.order.application.saga.domain.model.vo.Payload;
import com.example.order_service.order.application.saga.infrastructure.kafka.producer.SagaEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductStepHandler implements SagaStepHandler{
    private final SagaEventProducer producer;
    @Override
    public SagaStep getSagaStep() {
        return SagaStep.PRODUCT;
    }

    @Override
    public void process(Long sagaId, String orderNo, Payload payload) {
        producer.requestInventoryDeduction(sagaId, orderNo, payload);
    }

    @Override
    public void compensate(Long sagaId, String orderNo, Payload payload) {
        producer.requestInventoryCompensate(sagaId, orderNo, payload);
    }
}
