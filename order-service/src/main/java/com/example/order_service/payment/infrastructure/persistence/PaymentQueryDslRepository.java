package com.example.order_service.payment.infrastructure.persistence;

import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.repository.PaymentQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public class PaymentQueryDslRepository implements PaymentQueryRepository {
    private final JPAQueryFactory queryFactory;

    public PaymentQueryDslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Payment> findReadyPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }

    @Override
    public List<Payment> findRefundPendingPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }
}
