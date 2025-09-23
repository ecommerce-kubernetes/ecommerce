package com.example.order_service.service;

import com.example.order_service.service.event.OrderEndMessageEvent;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplicationMessageListener {
    private final SseConnectionService sseConnectionService;
    private final SagaManager sagaManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEndMessage(OrderEndMessageEvent event){
        sseConnectionService.send(event.getOrderId(), "order-end", event.getStatus());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePendingOrderCreated(PendingOrderCreatedEvent event){
        sagaManager.processPendingOrderSaga(event);
    }

}
