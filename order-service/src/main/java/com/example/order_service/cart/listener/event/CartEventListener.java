package com.example.order_service.cart.listener.event;

import com.example.order_service.order.application.event.PaymentCompletedEvent;
import com.example.order_service.cart.application.CartAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    private final CartAppService cartAppService;

    @EventListener
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event){
        cartAppService.removePurchasedItems(event.getUserId(), event.getProductVariantIds());
    }
}
