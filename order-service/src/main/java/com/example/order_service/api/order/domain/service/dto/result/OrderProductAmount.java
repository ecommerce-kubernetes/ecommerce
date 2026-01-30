package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderProductAmount {
    private long totalOriginalAmount;
    private long totalDiscountAmount;
    private long subTotalAmount;

    public static OrderProductAmount of(long totalOriginalAmount, long totalDiscountAmount, long subTotalAmount) {
        return OrderProductAmount.builder()
                .totalOriginalAmount(totalOriginalAmount)
                .totalDiscountAmount(totalDiscountAmount)
                .subTotalAmount(subTotalAmount)
                .build();
    }
}

