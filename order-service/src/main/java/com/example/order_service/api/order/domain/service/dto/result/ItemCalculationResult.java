package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemCalculationResult {
    private long totalOriginalPrice;
    private long totalProductDiscount;
    private long subTotalPrice;

    @Builder
    private ItemCalculationResult(long totalOriginalPrice, long totalProductDiscount, long subTotalPrice) {
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.subTotalPrice = subTotalPrice;
    }

    public static ItemCalculationResult of(long totalOriginalPrice, long totalProductDiscount, long subTotalPrice) {
        return ItemCalculationResult.builder()
                .totalOriginalPrice(totalOriginalPrice)
                .totalProductDiscount(totalProductDiscount)
                .subTotalPrice(subTotalPrice)
                .build();
    }
}
