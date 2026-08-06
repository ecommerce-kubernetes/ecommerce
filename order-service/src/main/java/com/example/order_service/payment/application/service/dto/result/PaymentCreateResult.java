package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.domain.PaymentStatus;
import lombok.Builder;

@Builder
public record PaymentCreateResult(
        Long paymentId,
        PaymentStatus status,
        Long orderId,
        String orderName,
        Money totalAmount
) {
    public static PaymentCreateResult from(PaymentResult payment, PaymentOrderResult order) {
        return PaymentCreateResult.builder()
                .paymentId(payment.paymentId())
                .status(payment.status())
                .orderId(order.orderId())
                .orderName(order.orderName())
                .totalAmount(payment.totalAmount())
                .build();
    }
}
