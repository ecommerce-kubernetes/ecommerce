package com.example.order_service.payment.application.listener;

import com.example.order_service.order.application.orchestrator.OrderSagaManger;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final OrderSagaManger orderSagaManger;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentComplete(PaymentCompleteEvent event) {
        orderSagaManger.startSaga(event.getOrderNo(), event.getPaymentKey());
    }
}
