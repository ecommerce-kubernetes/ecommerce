package com.example.order_service.cart.listener.event;

import com.example.order_service.api.order.facade.event.PaymentCompletedEvent;
import com.example.order_service.cart.application.CartFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    private final CartFacade cartFacade;

    @EventListener
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event){
        cartFacade.removePurchasedItems(event.getUserId(), event.getProductVariantIds());
    }
}
