package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentContextFactory {

    public CreatePaymentContext create(Long orderId, Long userId, Money totalAmount) {
        return CreatePaymentContext.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .build();
    }

    public ApprovePendingPaymentContext approvePending(Money amount, PaymentProvider provider, String paymentKey) {
        return ApprovePendingPaymentContext.builder()
                .amount(amount)
                .provider(provider)
                .paymentKey(paymentKey)
                .build();
    }

    public ApprovePaymentContext approve(PaymentMethod method, String transactionKey, Money amount, LocalDateTime occurredAt) {
        return ApprovePaymentContext.builder()
                .method(method)
                .transactionKey(transactionKey)
                .amount(amount)
                .occurredAt(occurredAt)
                .build();
    }

    public CancelPaymentContext cancel(String transactionKey, Money amount, LocalDateTime occurredAt, String reason) {
        return CancelPaymentContext.builder()
                .transactionKey(transactionKey)
                .amount(amount)
                .cancelReason(reason)
                .occurredAt(occurredAt)
                .build();
    }
}
