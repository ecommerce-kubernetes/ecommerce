package com.example.order_service.payment.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompleteEvent {
    private String orderNo;

    public static PaymentCompleteEvent of(String orderNo) {
        return PaymentCompleteEvent.builder()
                .orderNo(orderNo)
                .build();
    }
}
