package com.example.order_service.payment.application.service.dto.result;

import lombok.Builder;

@Builder
public record PaymentCreateResult(
        Long paymentId
) {
    public static PaymentCreateResult from(Long paymentId) {
        return PaymentCreateResult.builder()
                .paymentId(paymentId)
                .build();
    }
}
