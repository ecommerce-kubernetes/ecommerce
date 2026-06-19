package com.example.order_service.payment.application.service.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class PaymentContext {

    @Builder
    public record Create(
            Long userId,
            String orderNo,
            String paymentKey,
            Money totalAmount
    ) {
    }

    @Builder
    public record Approval(
            Long paymentId,
            PaymentStatus status,
            Money amount,
            PaymentMethod method,
            String transactionKey,
            LocalDateTime approvedAt
    ) {
    }

    @Builder
    public record Cancellation(
            Long paymentId,
            Money amount,
            PaymentStatus status,
            PaymentMethod method,
            String cancelReason,
            LocalDateTime approvedAt
    ) {
    }

}
