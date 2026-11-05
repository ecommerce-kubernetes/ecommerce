package com.example.order_service.payment.application.port;

import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long paymentId);
    Optional<Payment> findByIdAndUserId(Long paymentId, Long userId);
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    List<Payment> findPaymentsByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);
    List<Payment> findPaymentsByStatusAndUpdatedAtBefore(PaymentStatus status, LocalDateTime threshold);
}
