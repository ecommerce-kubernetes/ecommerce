package com.example.order_service.api.order.application.event;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentResultEvent {
    private Long orderId;
    private Long userId;
    private OrderEventStatus status;
    private OrderEventCode code;
    private List<Long> productVariantIds;
    private String failureReason;

    @Builder
    private PaymentResultEvent(Long orderId, Long userId, OrderEventStatus status, List<Long> productVariantIds, OrderEventCode code, String failureReason) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.productVariantIds = productVariantIds;
        this.code = code;
        this.failureReason = failureReason;
    }

    public static PaymentResultEvent of(Long orderId, Long userId, OrderEventStatus status, OrderEventCode code, List<Long> productVariantIds, String failureReason) {
        return PaymentResultEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .code(code)
                .productVariantIds(productVariantIds)
                .failureReason(failureReason)
                .build();
    }
}
