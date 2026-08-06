package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PaymentResult(
        Long paymentId,
        Long orderId,
        Long userId,
        PaymentStatus status,
        PaymentMethod method,
        PaymentProvider provider,
        List<PaymentTransactionResult> transactions,
        String paymentKey,
        Money totalAmount
) {

    @Builder
    public record PaymentTransactionResult(
            Long transactionId,
            String transactionKey,
            TransactionType type,
            Money amount,
            String reason,
            LocalDateTime occurredAt
    ) {
        public static PaymentTransactionResult from(PaymentTransaction paymentTransaction) {
            return PaymentTransactionResult.builder()
                    .transactionId(paymentTransaction.getId())
                    .transactionKey(paymentTransaction.getTransactionKey())
                    .type(paymentTransaction.getType())
                    .amount(paymentTransaction.getAmount())
                    .reason(paymentTransaction.getReason())
                    .occurredAt(paymentTransaction.getOccurredAt())
                    .build();
        }

        public static List<PaymentTransactionResult> from(List<PaymentTransaction> paymentTransactions) {
            return paymentTransactions.stream().map(PaymentTransactionResult::from).toList();
        }
    }

    public static PaymentResult from(Payment payment) {
        return PaymentResult.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .provider(payment.getProvider())
                .transactions(PaymentTransactionResult.from(payment.getPaymentTransactions()))
                .paymentKey(payment.getPaymentKey())
                .totalAmount(payment.getTotalAmount())
                .build();
    }
}
