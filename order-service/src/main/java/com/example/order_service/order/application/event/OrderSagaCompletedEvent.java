package com.example.order_service.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Deprecated
public class OrderSagaCompletedEvent {
    private String orderNo;

    public static OrderSagaCompletedEvent of(String orderNo) {
        return OrderSagaCompletedEvent.builder()
                .orderNo(orderNo)
                .build();
    }
}
