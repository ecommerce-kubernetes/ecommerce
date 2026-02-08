package com.example.order_service.api.order.infrastructure.client.payment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmRequest {
    private String orderId;
    private String paymentKey;
    private Long amount;

    @Builder
    private TossPaymentConfirmRequest(String orderId, String paymentKey, Long amount) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

    public static TossPaymentConfirmRequest of(String orderId, String paymentKey, Long amount) {
        return TossPaymentConfirmRequest.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .amount(amount)
                .build();
    }

}
