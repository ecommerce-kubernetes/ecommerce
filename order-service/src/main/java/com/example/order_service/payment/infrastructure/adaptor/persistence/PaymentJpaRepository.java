package com.example.order_service.payment.infrastructure.adaptor.persistence;

import com.example.order_service.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdAndUserId(Long paymentId, Long userId);
}
