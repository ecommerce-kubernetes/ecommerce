package com.example.order_service.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSagaFailedEvent {
    private String orderNo;
    private Long paymentId;
    private String code;

    public static OrderSagaFailedEvent of(String orderNo, Long paymentId, String code) {
        return OrderSagaFailedEvent.builder()
                .orderNo(orderNo)
                .paymentId(paymentId)
                .code(code)
                .build();
    }
}
