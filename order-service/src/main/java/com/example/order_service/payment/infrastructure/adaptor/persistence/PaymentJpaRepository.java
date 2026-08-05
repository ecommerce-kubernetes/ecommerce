package com.example.order_service.payment.infrastructure.adaptor.persistence;

import com.example.order_service.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
