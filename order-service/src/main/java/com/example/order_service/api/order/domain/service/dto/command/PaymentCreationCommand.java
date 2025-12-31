package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
public class PaymentCreationCommand {
    private Long orderId;
    private String paymentKey;
    private Long amount;
    private String method;
    private LocalDateTime approvedAt;

    @Builder
    private PaymentCreationCommand(Long orderId, String paymentKey, Long amount, String method, LocalDateTime approvedAt) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.method = method;
        this.approvedAt = approvedAt;
    }

    public static PaymentCreationCommand from(TossPaymentConfirmResponse response) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(response.getApprovedAt());
        return PaymentCreationCommand.builder()
                .orderId(response.getOrderId())
                .paymentKey(response.getPaymentKey())
                .amount(response.getTotalAmount())
                .method(response.getMethod())
                .approvedAt(offsetDateTime.toLocalDateTime())
                .build();
    }
}
