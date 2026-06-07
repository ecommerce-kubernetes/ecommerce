package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.event.OrderSagaCompletedEvent;
import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.payment.application.service.PaymentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final PaymentFacade paymentFacade;
    private final OrderCommandService orderCommandService;
    private final SagaMessageDispatcher dispatcher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSagaProcess(OrderSagaProcessEvent event) {
        SagaMessage message = SagaMessage.from(event);
        dispatcher.dispatch(message);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSagaComplete(OrderSagaCompletedEvent event) {
        orderCommandService.changeCompleted(event.getOrderNo());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderFail(OrderSagaFailedEvent event) {
        orderCommandService.changeFailed(event.getOrderNo(), event.getCode());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRefound(OrderSagaFailedEvent event) {
        paymentFacade.refound(event.getOrderNo(), event.getCode());
    }
}
