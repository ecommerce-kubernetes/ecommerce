package com.example.order_service.api.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentResultEvent {
    private Long orderId;
    private OrderEventStatus status;
    private OrderEventCode code;
    private String failureReason;

    @Builder
    private PaymentResultEvent(Long orderId, OrderEventStatus status, OrderEventCode code, String failureReason) {
        this.orderId = orderId;
        this.status = status;
        this.code = code;
        this.failureReason = failureReason;
    }

    public static PaymentResultEvent of(Long orderId, OrderEventStatus status, OrderEventCode code, String failureReason) {
        return PaymentResultEvent.builder()
                .orderId(orderId)
                .status(status)
                .code(code)
                .failureReason(failureReason)
                .build();
    }
}
