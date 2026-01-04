package com.example.order_service.api.order.application.event;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentResultEvent {
    private Long orderId;
    private Long userId;
    private OrderEventStatus status;
    private OrderFailureCode code;
    private List<Long> productVariantIds;

    @Builder
    private PaymentResultEvent(Long orderId, Long userId, OrderEventStatus status, List<Long> productVariantIds, OrderFailureCode code) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.productVariantIds = productVariantIds;
        this.code = code;
    }

    public static PaymentResultEvent of(Long orderId, Long userId, OrderEventStatus status, OrderFailureCode code, List<Long> productVariantIds) {
        return PaymentResultEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .code(code)
                .productVariantIds(productVariantIds)
                .build();
    }
}
