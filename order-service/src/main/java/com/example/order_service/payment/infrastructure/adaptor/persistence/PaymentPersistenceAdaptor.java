package com.example.order_service.payment.infrastructure.adaptor.persistence;

import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentPersistenceAdaptor implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }
}
