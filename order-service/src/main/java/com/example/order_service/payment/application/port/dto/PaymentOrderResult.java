package com.example.order_service.payment.application.port.dto;

import com.example.order_service.common.domain.vo.Money;

public record PaymentOrderResult(
        Long orderId,
        Money totalAmount
) {
}
