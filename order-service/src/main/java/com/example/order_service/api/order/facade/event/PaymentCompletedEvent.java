package com.example.order_service.api.order.facade.event;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaymentCompletedEvent {
    private String orderNo;
    private Long userId;
    private List<Long> productVariantIds;

    public static PaymentCompletedEvent of(String orderNo, Long userId, List<Long> productVariantIds) {
        return PaymentCompletedEvent.builder()
                .orderNo(orderNo)
                .userId(userId)
                .productVariantIds(productVariantIds)
                .build();
    }
}
