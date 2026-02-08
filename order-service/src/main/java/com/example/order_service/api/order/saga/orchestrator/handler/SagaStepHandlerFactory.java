package com.example.order_service.api.order.saga.orchestrator.handler;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SagaStepHandlerFactory {
    private final Map<SagaStep, SagaStepHandler> handlerMap;

    public SagaStepHandlerFactory(List<SagaStepHandler> handlers) {
        this.handlerMap = handlers.stream().collect(Collectors.toMap(SagaStepHandler::getSagaStep, Function.identity()));
    }

    public SagaStepHandler getHandler(SagaStep step) {
        SagaStepHandler handler = handlerMap.get(step);
        if (handler == null) {
            throw new IllegalArgumentException("사가 핸들러를 찾을 수 없음");
        }
        return handler;
    }
}
