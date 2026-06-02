package com.example.order_service.payment.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompleteEvent {
    private String orderNo;
    private String paymentKey;

    public static PaymentCompleteEvent of(String orderNo, String paymentKey) {
        return PaymentCompleteEvent.builder()
                .orderNo(orderNo)
                .paymentKey(paymentKey)
                .build();
    }
}
