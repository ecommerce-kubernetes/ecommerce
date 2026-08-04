package com.example.order_service.payment.api.web.dto.response;

import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record PaymentCreateResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long paymentId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long orderId,
        String orderName,
        Long totalAmount
) {
    public static PaymentCreateResponse from(PaymentCreateResult result) {
        return PaymentCreateResponse.builder()
                .paymentId(result.paymentId())
                .orderId(result.orderId())
                .orderName(result.orderName())
                .totalAmount(result.totalAmount().longValue())
                .build();
    }
}
