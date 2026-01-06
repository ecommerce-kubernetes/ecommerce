package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class ItemCalculationResult {
    private long totalOriginalPrice;
    private long totalProductDiscount;
    private long subTotalPrice;

    @Builder(access = AccessLevel.PRIVATE)
    private ItemCalculationResult(long totalOriginalPrice, long totalProductDiscount, long subTotalPrice) {
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.subTotalPrice = subTotalPrice;
    }

    private static ItemCalculationResult of(long totalOriginalPrice, long totalProductDiscount, long subTotalPrice) {
        return ItemCalculationResult.builder()
                .totalOriginalPrice(totalOriginalPrice)
                .totalProductDiscount(totalProductDiscount)
                .subTotalPrice(subTotalPrice)
                .build();
    }

    public static ItemCalculationResult of(Map<Long, Integer> quantityByVariantId, Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId){
        long totalOriginPrice = 0;
        long totalProductDiscount = 0;
        long subTotalPrice = 0;
        for (Map.Entry<Long, Integer> entry : quantityByVariantId.entrySet()) {
            Long productVariantId = entry.getKey();
            OrderProductResponse.UnitPrice unitPrice = unitPriceByVariantId.get(productVariantId);

            totalOriginPrice += unitPrice.getOriginalPrice() * entry.getValue();
            totalProductDiscount += unitPrice.getDiscountAmount() * entry.getValue();
            subTotalPrice += unitPrice.getDiscountedPrice() * entry.getValue();
        }
        return of(totalOriginPrice, totalProductDiscount, subTotalPrice);
    }
}
