package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final SagaMessageDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSagaProcess(OrderSagaProcessEvent event) {
        SagaMessage message = SagaMessage.from(event);
        dispatcher.dispatch(message);
    }
}
