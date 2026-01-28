package com.example.order_service.api.order.facade.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompletedEvent {
    private String orderNo;
    private Long userId;

    public static PaymentCompletedEvent of(String orderNo, Long userId) {
        return PaymentCompletedEvent.builder()
                .orderNo(orderNo)
                .userId(userId)
                .build();
    }
}
