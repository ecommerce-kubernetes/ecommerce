package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record PaymentCreateResult(
        Long paymentId,
        Long orderId,
        String orderName,
        Money totalAmount
) {
}
