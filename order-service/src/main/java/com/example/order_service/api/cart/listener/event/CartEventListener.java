package com.example.order_service.api.cart.listener.event;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    //TODO 주문시 해당 상품 장바구니 삭제
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
    }
}
