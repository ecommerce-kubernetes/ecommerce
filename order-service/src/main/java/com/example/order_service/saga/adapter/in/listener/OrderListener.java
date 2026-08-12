package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.order.domain.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderListener {

    @TransactionalEventListener
    public void handleOrderPaidEvent(OrderPaidEvent event) {

    }
}
