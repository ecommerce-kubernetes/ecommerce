package com.example.order_service.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSagaFailedEvent {
    private String orderNo;
    private String code;

    public static OrderSagaFailedEvent of(String orderNo, String code) {
        return OrderSagaFailedEvent.builder()
                .orderNo(orderNo)
                .code(code)
                .build();
    }
}
