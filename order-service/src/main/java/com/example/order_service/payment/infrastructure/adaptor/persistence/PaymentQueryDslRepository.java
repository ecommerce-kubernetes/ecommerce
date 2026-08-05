package com.example.order_service.payment.infrastructure.adaptor.persistence;

import com.example.order_service.payment.domain.Payment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public class PaymentQueryDslRepository {
    private final JPAQueryFactory queryFactory;

    public PaymentQueryDslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Payment> findReadyPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }

    public List<Payment> findRefundPendingPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }
}
