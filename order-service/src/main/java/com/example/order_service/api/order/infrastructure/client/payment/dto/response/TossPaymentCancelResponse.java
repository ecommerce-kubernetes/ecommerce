package com.example.order_service.api.order.infrastructure.client.payment.dto.response;

public class TossPaymentCancelResponse {
    private String paymentKey;
    private Long orderId;
    private Long totalAmount;
    private String status;
    private String method;
    private String approvedAt;
}
