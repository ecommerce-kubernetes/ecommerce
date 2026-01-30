package com.example.order_service.api.order.saga.orchestrator.handler;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.orchestrator.event.SagaResourceSecuredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStepHandler implements SagaStepHandler {

    private ApplicationEventPublisher publisher;

    @Override
    public SagaStep getSagaStep() {
        return SagaStep.PAYMENT;
    }

    @Override
    public void process(Long sagaId, String orderNo, Payload payload) {
        SagaResourceSecuredEvent event = SagaResourceSecuredEvent.of(sagaId, orderNo, payload.getUserId());
        publisher.publishEvent(event);
    }

    @Override
    public void compensate(Long sagaId, String orderNo, Payload payload) {

    }
}
