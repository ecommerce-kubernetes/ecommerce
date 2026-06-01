package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentRecord;
import com.example.order_service.payment.domain.model.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class PaymentResult {

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
                    .orderNo(payment.getOrderNo())
                    .paymentKey(payment.getPaymentKey())
                    .totalAmount(payment.getTotalAmount())
                    .status(payment.getStatus())
                    .build();
        }
    }

    @Builder
    public record PaymentApproval(
            String paymentKey,
            String orderNo,
            Money totalAmount,
            PaymentMethod method,
            PaymentStatus status,
            LocalDateTime approvedAt
    ) {
        public static PaymentApproval of(Payment payment, PaymentRecord paymentRecord) {
            return PaymentApproval.builder()
                    .paymentKey(payment.getPaymentKey())
                    .orderNo(payment.getOrderNo())
                    .totalAmount(payment.getTotalAmount())
                    .method(paymentRecord.getMethod())
                    .status(payment.getStatus())
                    .approvedAt(paymentRecord.getApprovedAt())
                    .build();
        }
    }
}
