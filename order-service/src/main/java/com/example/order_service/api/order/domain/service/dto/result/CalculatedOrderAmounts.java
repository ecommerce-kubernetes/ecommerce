package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalculatedOrderAmounts {
    private long totalOriginalAmount;
    private long totalProductDiscount;
    private long couponDiscountAmount;
    private long usePointAmount;
    private long finalPaymentAmount;

    public static CalculatedOrderAmounts of(long totalOriginalPrice, long totalProductDiscount, long couponDiscount, long pointDiscount, long finalPaymentAmount) {
        return CalculatedOrderAmounts.builder()
                .totalOriginalAmount(totalOriginalPrice)
                .totalProductDiscount(totalProductDiscount)
                .couponDiscountAmount(couponDiscount)
                .usePointAmount(pointDiscount)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }
}
