package com.example.order_service.payment.application.external.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PGPaymentResult {

    @Builder
    public record Approval(
            PaymentStatus status,
            Money totalAmount,
            PaymentMethod method,
            String transactionKey,
            LocalDateTime approvedAt
    ) {
    }

    @Builder
    public record Cancellation(
            PaymentStatus status,
            List<CancelReceipt> cancels
    ) {
        public CancelReceipt lastCancel() {
            return cancels.getLast();
        }
    }

    @Builder
    public record CancelReceipt(
            String transactionKey,
            Money cancelAmount,
            String cancelReason,
            LocalDateTime canceledAt
    ) {
    }

    @Builder
    public record Inquiry(
            String paymentKey,
            String orderNo,
            PaymentStatus status,
            Money totalAmount,
            Money balanceAmount,
            PaymentMethod method,
            String lastTransactionKey,
            LocalDateTime approvedAt,
            FailureReason failure,
            List<CancelReceipt> cancels
    ) {
        public CancelReceipt lastCancel() {
            return cancels.getLast();
        }
    }

    @Builder
    public record FailureReason(
            String code,
            String message
    ) {
    }
}
