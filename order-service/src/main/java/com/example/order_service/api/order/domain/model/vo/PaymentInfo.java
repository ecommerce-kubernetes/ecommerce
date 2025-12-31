package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.model.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentInfo {
    private Long id;
    private String paymentKey;
    private Long amount;
    private String method;
    private LocalDateTime approvedAt;

    @Builder
    private PaymentInfo(Long id, String paymentKey, Long amount, String method, LocalDateTime approvedAt) {
        this.id = id;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.method = method;
        this.approvedAt = approvedAt;
    }

    public static PaymentInfo from(Payment payment){
        if (payment == null) {
            return null;
        }
        return PaymentInfo.builder()
                .id(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}
