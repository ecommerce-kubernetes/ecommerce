package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentTransaction;
import com.example.order_service.payment.domain.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Deprecated
public class PaymentResultDeprecated {

    @Builder
    public record Default(
            Long id,
            String orderNo,
            String paymentKey,
            Money totalAmount,
            PaymentStatus status
    ) {
        public static Default from(Payment payment) {
            return Default.builder()
                    .id(payment.getId())
                    .orderNo(null)
                    .paymentKey(payment.getPaymentKey())
                    .totalAmount(payment.getTotalAmount())
                    .status(payment.getStatus())
                    .build();
        }
    }

    @Builder
    public record PaymentApproval(
            Long paymentId,
            String paymentKey,
            String orderNo,
            Money totalAmount,
            PaymentMethod method,
            PaymentStatus status,
            LocalDateTime approvedAt
    ) {
        public static PaymentApproval of(Payment payment, PaymentTransaction paymentTransaction) {
            return PaymentApproval.builder()
                    .paymentKey(payment.getPaymentKey())
                    .orderNo(null)
                    .totalAmount(payment.getTotalAmount())
                    .method(payment.getMethod())
                    .status(payment.getStatus())
                    .approvedAt(paymentTransaction.getOccurredAt())
                    .build();
        }
    }

    @Builder
    public record PaymentCancel(
            String paymentKey,
            String orderNo,
            Money canceledAmount,
            PaymentStatus status,
            LocalDateTime canceledAt
    ) {
        public static PaymentCancel of(Payment payment, PaymentTransaction paymentTransaction) {
            return PaymentCancel.builder()
                    .paymentKey(payment.getPaymentKey())
                    .orderNo(null)
                    .canceledAmount(paymentTransaction.getAmount())
                    .status(payment.getStatus())
                    .canceledAt(paymentTransaction.getOccurredAt())
                    .build();
        }
    }
}
