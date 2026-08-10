package com.example.order_service.payment.application.service.dto.result;

import lombok.Builder;

@Builder
public record PaymentConfirmResult(
        Long paymentId
) {
    public static PaymentConfirmResult of(Long paymentId) {
        return PaymentConfirmResult.builder()
                .paymentId(paymentId)
                .build();
    }
}
