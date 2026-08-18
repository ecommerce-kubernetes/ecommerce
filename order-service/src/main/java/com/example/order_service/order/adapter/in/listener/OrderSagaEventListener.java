package com.example.order_service.order.adapter.in.listener;

import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.saga.domain.event.SagaProcessingFailedEvent;
import com.example.order_service.saga.domain.event.SagaSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderSagaEventListener {

    private OrderCommandService orderCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSagaSuccessEvent(SagaSuccessEvent event) {
        orderCommandService.changeCompleted(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSagaProcessingFailedEvent(SagaProcessingFailedEvent event) {
        orderCommandService.changeFailed(event.orderId(), event.failureReason());
    }
}
