package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.time.LocalDateTime;

public class PaymentResult {

    @Builder
    public record PaymentApproval(
            String paymentKey,
            String orderNo,
            Money totalAmount,
            String method,
            String status,
            LocalDateTime approvedAt
    ) {
    }
}
