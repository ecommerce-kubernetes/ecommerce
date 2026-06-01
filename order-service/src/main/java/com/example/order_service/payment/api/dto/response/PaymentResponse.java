package com.example.order_service.payment.api.dto.response;

import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

public class PaymentResponse {

    @Builder
    public record PaymentApproval(
            String paymentKey,
            String orderNo,
            Long totalAmount,
            PaymentMethod method,
            PaymentStatus status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime approvedAt
    ) {
        public static PaymentApproval from(PaymentResult.PaymentApproval result) {
            return PaymentApproval.builder()
                    .paymentKey(result.paymentKey())
                    .orderNo(result.orderNo())
                    .totalAmount(result.totalAmount().longValue())
                    .method(result.method())
                    .status(result.status())
                    .approvedAt(result.approvedAt())
                    .build();
        }
    }
}
