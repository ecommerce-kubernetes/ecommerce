package com.example.order_service.api.order.saga.listener;

import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderApplicationService orderApplicationService;
    private final SagaManager sagaManager;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        SagaStartCommand command = SagaStartCommand.from(event);
        sagaManager.startSaga(command);
    }

    @EventListener
    public void handleSagaCompleted(SagaCompletedEvent event){
        orderApplicationService.changePaymentWaiting(event.getOrderId());
    }

    @EventListener
    public void handleSagaAborted(SagaAbortEvent event) {
        orderApplicationService.changeCanceled(event.getOrderId(), event.getOrderFailureCode());
    }
}
