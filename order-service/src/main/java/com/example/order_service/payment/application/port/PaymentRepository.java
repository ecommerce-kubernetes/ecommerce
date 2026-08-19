package com.example.order_service.payment.application.port;

import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentStatus;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByIdAndUserId(Long paymentId, Long userId);
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
