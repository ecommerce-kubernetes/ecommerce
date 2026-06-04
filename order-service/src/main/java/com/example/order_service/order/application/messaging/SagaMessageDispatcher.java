package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SagaMessageDispatcher {
    private Map<SagaStep, SagaMessageHandler> forwardHandlers;
    private Map<SagaStep, SagaMessageHandler> compensationHandlers;

    public SagaMessageDispatcher(List<SagaMessageHandler> handlers) {
        this.forwardHandlers = handlers.stream()
                .collect(Collectors.toMap(SagaMessageHandler::supportsForward, Function.identity()));
        this.compensationHandlers = handlers.stream()
                .filter(handler -> handler.supportsCompensation() != null)
                .collect(Collectors.toMap(SagaMessageHandler::supportsCompensation, Function.identity()));
    }

    public void dispatch(SagaMessage message){
        if (message.getStatus() == SagaStatus.COMPENSATING) {
            SagaMessageHandler compensateHandler = compensationHandlers.get(message.getStep());
            compensateHandler.compensate(message);
            return;
        }
        SagaMessageHandler handler = forwardHandlers.get(message.getStep());
        handler.forward(message);
    }
}
