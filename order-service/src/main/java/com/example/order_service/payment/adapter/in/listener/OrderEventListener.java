package com.example.order_service.payment.adapter.in.listener;

import com.example.order_service.order.domain.order.event.OrderFailedEvent;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentCancelCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final PaymentFacade paymentFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFailedOrderEvent(OrderFailedEvent event) {
        PaymentCancelCommand command = PaymentCancelCommand.of(event.orderId(), event.userId(), event.reason());
        paymentFacade.cancel(command);
    }
}
