package com.example.order_service.api.order.facade.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentFailedEvent {
    private String orderNo;
    private Long userId;
    private PaymentFailureCode code;

    public static PaymentFailedEvent of(String orderNo, Long userId, PaymentFailureCode code) {
        return PaymentFailedEvent.builder()
                .orderNo(orderNo)
                .userId(userId)
                .code(code)
                .build();
    }
}
