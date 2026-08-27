package com.example.order_service.payment.application.service.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.domain.PaymentStatus;
import lombok.Builder;

@Builder
public record PaymentCreateResult(
        Long paymentId
) {
    public static PaymentCreateResult from(Long paymentId) {
        return PaymentCreateResult.builder()
                .paymentId(paymentId)
                .build();
    }
}
