package com.example.order_service.api.order.saga.orchestrator.handler;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponStepHandler implements SagaStepHandler {
    private final SagaEventProducer producer;
    @Override
    public SagaStep getSagaStep() {
        return SagaStep.COUPON;
    }

    @Override
    public void process(Long sagaId, String orderNo, Payload payload) {
        producer.requestCouponUse(sagaId, orderNo, payload);
    }

    @Override
    public void compensate(Long sagaId, String orderNo, Payload payload) {
        producer.requestCouponCompensate(sagaId, orderNo, payload);
    }
}
