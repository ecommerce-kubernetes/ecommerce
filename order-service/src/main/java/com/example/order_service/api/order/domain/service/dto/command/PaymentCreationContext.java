package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.domain.model.PaymentMethod;
import com.example.order_service.api.order.domain.model.PaymentStatus;
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
