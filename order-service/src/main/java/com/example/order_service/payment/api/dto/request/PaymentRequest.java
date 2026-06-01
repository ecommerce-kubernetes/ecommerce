package com.example.order_service.payment.api.dto.request;

import lombok.Builder;

public class PaymentRequest {

    @Builder
    public record Confirm(
            String orderNo,
            String paymentKey,
            Long amount
    ) {
    }
}
