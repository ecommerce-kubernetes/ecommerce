package com.example.order_service.payment.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentResultFixture {

    public static PaymentResult.PaymentResultBuilder anPaymentResult() {
        PaymentResult.PaymentTransactionResult transactionResult = anTransactionResult().build();
        return PaymentResult.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .provider(PaymentProvider.TOSS)
                .transactions(List.of(transactionResult))
                .paymentKey("paymentKey")
                .totalAmount(Money.wons(1000L));
    }

    public static PaymentResult.PaymentTransactionResult.PaymentTransactionResultBuilder anTransactionResult() {
        return PaymentResult.PaymentTransactionResult.builder()
                .transactionId(1L)
                .transactionKey("transactionKey")
                .type(TransactionType.PAYMENT)
                .amount(Money.wons(1000L))
                .reason("정상 승인")
                .occurredAt(LocalDateTime.now());
    }
}
