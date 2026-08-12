package com.example.order_service.order.adapter.in.listener;

import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.payment.domain.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderCommandService orderCommandService;

    @TransactionalEventListener
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        orderCommandService.changePaid(event.orderId());
    }
}
