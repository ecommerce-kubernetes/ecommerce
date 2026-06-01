package com.example.order_service.payment.api.dto.response;

import java.time.LocalDateTime;

public class PaymentResponse {

    public record PaymentApproval(
            String paymentKey,
            String orderNo,
            Long totalAmount,
            String method,
            String status,
            LocalDateTime approvedAt
    ) {
    }
}
