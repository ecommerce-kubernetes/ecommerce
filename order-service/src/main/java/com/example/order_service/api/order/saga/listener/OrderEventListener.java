package com.example.order_service.api.order.saga.listener;

import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.application.event.PaymentResultEvent;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaPaymentCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaResourceSecuredEvent;
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
    public void handlePaymentResult(PaymentResultEvent event) {
        SagaPaymentCommand command = SagaPaymentCommand.from(event);
        sagaManager.processPaymentResult(command);
    }

    @EventListener
    public void handleSagaCompleted(SagaResourceSecuredEvent event){
        orderApplicationService.preparePayment(event.getOrderNo());
    }

    @EventListener
    public void handleSagaAborted(SagaAbortEvent event) {
        orderApplicationService.processOrderFailure(event.getOrderNo(), event.getOrderFailureCode());
    }
}
