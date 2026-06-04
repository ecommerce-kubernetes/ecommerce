package com.example.order_service.order.application.listener;

import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SagaEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSagaProcess(OrderSagaProcessEvent event) {

    }
}
