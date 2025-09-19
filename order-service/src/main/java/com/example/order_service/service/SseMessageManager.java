package com.example.order_service.service;

import com.example.order_service.service.event.OrderEndMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseMessageManager {
    private final SseConnectionService sseConnectionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEndMessage(OrderEndMessageEvent event){
        sseConnectionService.send(event.getOrderId(), "order-end", event.getStatus());
    }
}
