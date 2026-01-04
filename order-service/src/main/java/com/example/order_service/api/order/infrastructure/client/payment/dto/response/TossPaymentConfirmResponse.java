package com.example.order_service.api.order.infrastructure.client.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmResponse {
    private String paymentKey;
    private String orderId;
    private Long totalAmount;
    private String status;
    private String method;
    private String approvedAt;

    @Builder
    private TossPaymentConfirmResponse(String paymentKey, String orderId, Long totalAmount, String status, String method, String approvedAt) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.method = method;
        this.approvedAt = approvedAt;
    }
}
