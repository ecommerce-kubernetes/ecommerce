package com.example.order_service.api.order.facade.event;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentResultEvent {
    private String orderNo;
    private Long userId;
    private OrderEventStatus status;
    private OrderFailureCode code;
    private List<Long> productVariantIds;

    @Builder
    private PaymentResultEvent(String orderNo, Long userId, OrderEventStatus status, List<Long> productVariantIds, OrderFailureCode code) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.status = status;
        this.productVariantIds = productVariantIds;
        this.code = code;
    }

    public static PaymentResultEvent of(String orderNo, Long userId, OrderEventStatus status, OrderFailureCode code, List<Long> productVariantIds) {
        return PaymentResultEvent.builder()
                .orderNo(orderNo)
                .userId(userId)
                .status(status)
                .code(code)
                .productVariantIds(productVariantIds)
                .build();
    }
}
