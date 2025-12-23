package com.example.order_service.api.order.application.event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderResultEvent {
    private Long orderId;
    private Long userId;
    private OrderResultStatus status;
    private OrderResultCode code;
    private String orderName;
    private Long finalPaymentAmount;
    private String message;

    @Builder
    private OrderResultEvent(Long orderId, Long userId, OrderResultStatus status, OrderResultCode code, String orderName,
                             Long finalPaymentAmount, String message) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.code = code;
        this.orderName = orderName;
        this.finalPaymentAmount = finalPaymentAmount;
        this.message = message;
    }

    public static OrderResultEvent of(Long orderId, Long userId, OrderResultStatus status, OrderResultCode code,
                                      String orderName, Long finalPaymentAmount, String message) {
        return OrderResultEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .code(code)
                .orderName(orderName)
                .finalPaymentAmount(finalPaymentAmount)
                .message(message)
                .build();
    }
}
