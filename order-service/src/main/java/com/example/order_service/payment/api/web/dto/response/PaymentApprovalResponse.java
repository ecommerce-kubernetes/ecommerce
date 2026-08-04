package com.example.order_service.payment.api.web.dto.response;

import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import lombok.Builder;

@Builder
public record PaymentApprovalResponse(
        Long paymentId
) {
    public static PaymentApprovalResponse from(PaymentResult.PaymentApproval result) {
        return PaymentApprovalResponse.builder()
                .paymentId(result.paymentId())
                .build();
    }
}
