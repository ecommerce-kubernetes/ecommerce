package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class VariantResponse {
    private Long variantId;
    private String sku;
    private List<Long> optionValueIds;
    private Long originalPrice;
    private Long discountedPrice;
    private Integer discountRate;
    private Integer stockQuantity;


    @Builder
    public VariantResponse(Long variantId, String sku, List<Long> optionValueIds, Long originalPrice, Long discountedPrice, Integer discountRate, Integer stockQuantity) {
        this.variantId = variantId;
        this.sku = sku;
        this.optionValueIds = optionValueIds;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.discountRate = discountRate;
        this.stockQuantity = stockQuantity;
    }
}
