package com.example.order_service.payment.adapter.out.persistence;

import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdAndUserId(Long paymentId, Long userId);
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    List<Payment> findPaymentsByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);
}
