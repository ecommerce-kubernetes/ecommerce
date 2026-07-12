package com.example.order_service.cart.listener.event;

import com.example.order_service.cart.application.facade.CartFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    private final CartFacade cartFacade;
}
