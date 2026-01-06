package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
public class PaymentCreationCommand {
    private String orderNo;
    private String paymentKey;
    private Long amount;
    private String method;
    private LocalDateTime approvedAt;

    @Builder
    private PaymentCreationCommand(String orderNo, String paymentKey, Long amount, String method, LocalDateTime approvedAt) {
        this.orderNo = orderNo;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.method = method;
        this.approvedAt = approvedAt;
    }

    public static PaymentCreationCommand from(TossPaymentConfirmResponse response) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(response.getApprovedAt());
        return PaymentCreationCommand.builder()
                .orderNo(response.getOrderId())
                .paymentKey(response.getPaymentKey())
                .amount(response.getTotalAmount())
                .method(response.getMethod())
                .approvedAt(offsetDateTime.toLocalDateTime())
                .build();
    }
}
