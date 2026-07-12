package com.example.order_service.payment.infrastructure.persistence;

import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.payment.domain.repository.PaymentQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.payment.domain.model.QPayment.payment;


@Repository
public class PaymentQueryDslRepository implements PaymentQueryRepository {
    private final JPAQueryFactory queryFactory;

    public PaymentQueryDslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Payment> findReadyPaymentsBefore(LocalDateTime threshold, int size) {
        return queryFactory
                .selectFrom(payment)
                .where(
                        payment.status.eq(PaymentStatus.READY),
                        payment.createdAt.before(threshold)
                )
                .orderBy(payment.createdAt.asc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<Payment> findRefundPendingPaymentsBefore(LocalDateTime threshold, int size) {
        return queryFactory
                .selectFrom(payment)
                .where(
                        payment.status.eq(PaymentStatus.REFUND_PENDING),
                        payment.refundPendingAt.before(threshold)
                )
                .orderBy(payment.refundPendingAt.asc())
                .limit(size)
                .fetch();
    }
}
