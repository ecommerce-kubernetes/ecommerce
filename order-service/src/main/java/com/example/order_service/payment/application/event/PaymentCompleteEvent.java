package com.example.order_service.payment.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompleteEvent {
    private String orderNo;
    private Long paymentId;

    public static PaymentCompleteEvent of(String orderNo, Long paymentId) {
        return PaymentCompleteEvent.builder()
                .orderNo(orderNo)
                .paymentId(paymentId)
                .build();
    }
}
