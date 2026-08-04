package com.example.order_service.payment.api.web.dto.response;

import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

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
