package com.example.order_service.payment.api.web.dto.response;

import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResultDeprecated;
import lombok.Builder;

@Builder
public record PaymentApprovalResponse(
        Long paymentId
) {
    public static PaymentApprovalResponse from(PaymentConfirmResult result) {
        return PaymentApprovalResponse.builder()
                .paymentId(result.paymentId())
                .build();
    }
}
