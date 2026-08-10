package com.example.order_service.order.infrastructure.adaptor.listener;

import com.example.order_service.order.application.service.order.OrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderCommandService orderCommandService;

    public void changeOrderStatusToPaid() {
    }
}
