package com.example.order_service.api.cart.listener.event;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
    }
}
