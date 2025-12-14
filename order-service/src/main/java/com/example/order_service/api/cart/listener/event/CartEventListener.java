package com.example.order_service.api.cart.listener.event;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
    }
}
