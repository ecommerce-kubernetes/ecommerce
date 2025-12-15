package com.example.order_service.api.order.saga.listener;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventSagaListener {

    private final SagaManager sagaManager;

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        SagaStartCommand command = SagaStartCommand.from(event);
        sagaManager.startSaga(command);
    }
}
