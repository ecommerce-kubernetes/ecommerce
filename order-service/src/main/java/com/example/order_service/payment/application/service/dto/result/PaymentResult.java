package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.payment.domain.PaymentStatus;
import lombok.Builder;

@Builder
public record PaymentResult(
    Long paymentId,
    Long orderId,
    Long userId,
    PaymentStatus status

) {
}
