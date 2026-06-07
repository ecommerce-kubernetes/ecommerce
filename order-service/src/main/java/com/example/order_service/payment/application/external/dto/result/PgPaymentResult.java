package com.example.order_service.payment.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class PgPaymentResult {

    @Builder
    public record Approval(
           String orderNo,
           String paymentKey,
           Money totalAmount,
           PaymentStatus status,
           PaymentMethod method,
           LocalDateTime approvedAt
    ) {}

    @Builder
    public record Cancellation(
            String orderNo,
            String paymentKey,
            Money totalAmount,
            PaymentStatus status,
            PaymentMethod method,
            LocalDateTime approvedAt
    ) {}
}
