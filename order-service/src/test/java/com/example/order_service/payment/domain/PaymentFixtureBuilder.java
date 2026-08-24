package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentFixtureBuilder {

    private AtomicLong idSeq = new AtomicLong(100L);
    private IdGenerator idGenerator = idSeq::getAndIncrement;

    private Long orderId = 1L;
    private Long userId = 1L;
    private Money totalAmount = Money.wons(1000L);

    private TargetState targetState = TargetState.READY;
    private PaymentProvider provider = PaymentProvider.TOSS;
    private PaymentMethod method = PaymentMethod.CARD;

    private enum TargetState {
        READY, APPROVAL_PENDING, DONE, REFUND_PENDING
    }

    public static PaymentFixtureBuilder given() {
        return new PaymentFixtureBuilder();
    }

    public PaymentFixtureBuilder withTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public PaymentFixtureBuilder asApprovePending() {
        this.targetState = TargetState.APPROVAL_PENDING;
        return this;
    }

    public PaymentFixtureBuilder asDone() {
        this.targetState = TargetState.DONE;
        return this;
    }

    public PaymentFixtureBuilder asRefundPending() {
        this.targetState = TargetState.REFUND_PENDING;
        return this;
    }

    public Payment build() {
        CreatePaymentContext context = CreatePaymentContext.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .build();

        Payment payment = Payment.create(context, idGenerator);

        if (targetState == TargetState.READY) {
            return payment;
        }

        ApprovePendingPaymentContext pendingContext = ApprovePendingPaymentContext.builder()
                .provider(provider)
                .paymentKey("paymentKey-123")
                .amount(totalAmount)
                .build();
        payment.approvePending(pendingContext);

        if (targetState == TargetState.APPROVAL_PENDING) {
            return payment;
        }

        ApprovePaymentContext approveContext = ApprovePaymentContext.builder()
                .method(method)
                .transactionKey("tx-key-123")
                .amount(totalAmount)
                .occurredAt(LocalDateTime.now())
                .build();
        payment.approve(approveContext, idGenerator);

        if (targetState == TargetState.DONE) {
            return payment;
        }

        payment.refundPending();

        return payment;
    }
}
