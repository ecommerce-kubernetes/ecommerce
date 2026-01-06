package com.example.order_service.api.order.infrastructure.client.payment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmRequest {
    private String orderNo;
    private String paymentKey;
    private Long amount;

    @Builder
    private TossPaymentConfirmRequest(String orderNo, String paymentKey, Long amount) {
        this.orderNo = orderNo;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

    public static TossPaymentConfirmRequest of(String orderNo, String paymentKey, Long amount) {
        return TossPaymentConfirmRequest.builder()
                .orderNo(orderNo)
                .paymentKey(paymentKey)
                .amount(amount)
                .build();
    }

}
