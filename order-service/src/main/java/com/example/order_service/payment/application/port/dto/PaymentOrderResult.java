package com.example.order_service.payment.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record PaymentOrderResult(
        Long orderId,
        PaymentOrderStatus status,
        String orderName,
        Money totalAmount
) {
}
