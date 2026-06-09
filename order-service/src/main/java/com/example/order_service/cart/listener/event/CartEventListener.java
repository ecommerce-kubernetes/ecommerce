package com.example.order_service.cart.listener.event;

import com.example.order_service.cart.application.CartAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartEventListener {

    private final CartAppService cartAppService;
}
