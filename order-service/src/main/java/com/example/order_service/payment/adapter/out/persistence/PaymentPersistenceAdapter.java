package com.example.order_service.payment.adapter.out.persistence;

import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> findByIdAndUserId(Long paymentId, Long userId) {
        return paymentJpaRepository.findByIdAndUserId(paymentId, userId);
    }

    @Override
    public Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status) {
        return paymentJpaRepository.findByOrderIdAndStatus(orderId, status);
    }

    @Override
    public List<Payment> findPaymentsByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold) {
        return paymentJpaRepository.findPaymentsByStatusAndCreatedAtBefore(status, threshold);
    }
}
