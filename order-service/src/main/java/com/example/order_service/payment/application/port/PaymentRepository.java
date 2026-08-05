package com.example.order_service.payment.application.port;

import com.example.order_service.payment.domain.Payment;


public interface PaymentRepository {
    Payment save(Payment payment);
}
