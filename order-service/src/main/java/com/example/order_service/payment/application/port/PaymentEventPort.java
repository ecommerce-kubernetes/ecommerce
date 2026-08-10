package com.example.order_service.payment.application.port;

import com.example.order_service.payment.domain.event.PaymentApprovedEvent;

public interface PaymentEventPort {

    void publishApproved(PaymentApprovedEvent event);
}
