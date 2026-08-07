package com.example.order_service.payment.application.service.dto.result;

import lombok.Builder;

@Builder
public record PaymentConfirmResult(
        Long paymentId
) {
}
