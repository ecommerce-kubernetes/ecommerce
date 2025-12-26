package com.example.order_service.api.order.infrastructure.client.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmResponse {
    private String paymentKey;
    private Long orderId;
    private Long totalAmount;
    private String status;
    private String approvedAt;
}
