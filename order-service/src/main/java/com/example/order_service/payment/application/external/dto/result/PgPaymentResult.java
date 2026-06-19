package com.example.order_service.payment.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PgPaymentResult {

    @Builder
    public record Approval(
           PaymentStatus status,
           Money totalAmount,
           PaymentMethod method,
           String lastTransactionKey,
           LocalDateTime approvedAt
    ) {}

    @Builder
    public record Cancellation(
            PaymentStatus status,
            List<CancelReceipt> cancels
    ) {}

    @Builder
    public record CancelReceipt(
            String transactionKey,
            Money cancelAmount,
            String cancelReason,
            LocalDateTime canceledAt
    ) {
    }
}
