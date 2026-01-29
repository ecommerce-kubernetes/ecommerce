package com.example.order_service.api.cart.listener.event;

import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.order.facade.event.PaymentCompletedEvent;
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
