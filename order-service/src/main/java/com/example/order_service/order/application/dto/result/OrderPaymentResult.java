package com.example.order_service.order.application.dto.result;

import com.example.order_service.order.domain.model.vo.PaymentMethod;
import com.example.order_service.order.domain.model.vo.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class OrderPaymentResult {

    @Builder
    public record Payment(
            String orderNo,
            String paymentKey,
            Long totalAmount,
            PaymentStatus status,
            PaymentMethod method,
            LocalDateTime approvedAt
    ) {
    }
}
