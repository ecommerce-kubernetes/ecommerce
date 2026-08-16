package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.order.domain.order.event.OrderPaidEvent;
import com.example.order_service.saga.application.service.OrderSagaCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderListener {

    private final OrderSagaCommandService orderSagaCommandService;

    @TransactionalEventListener
    public void handleOrderPaidEvent(OrderPaidEvent event) {

    }
}
