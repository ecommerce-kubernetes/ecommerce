package com.example.order_service.order.domain.service.dto.command;

import com.example.order_service.order.application.dto.result.PaymentMethod;
import com.example.order_service.order.application.dto.result.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentCreationContext {
    private String orderNo;
    private String paymentKey;
    private Long amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDateTime approvedAt;
}
