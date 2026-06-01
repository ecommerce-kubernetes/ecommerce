package com.example.order_service.payment.application.service.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentContext(
        Long userId,
        String orderNo,
        String paymentKey,
        Money amount,
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime approvedAt
) {
}
