package com.example.order_service.payment.adapter.in.web.dto.response;

import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record PaymentCreateResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long paymentId
) {
    public static PaymentCreateResponse from(PaymentCreateResult result) {
        return PaymentCreateResponse.builder()
                .paymentId(result.paymentId())
                .build();
    }
}
