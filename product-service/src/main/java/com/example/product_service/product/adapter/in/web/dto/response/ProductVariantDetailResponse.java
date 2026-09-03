package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductVariantDetailResponse(
        Long variantId,
        String sku,
        List<Long> optionValueIds,
        Long originalPrice,
        Long discountedPrice,
        Integer discountRate,
        Integer stockQuantity
) {
    public static ProductVariantDetailResponse from(ProductResult.VariantDetail result) {
        return ProductVariantDetailResponse.builder()
                .variantId(result.variantId())
                .sku(result.sku())
                .optionValueIds(result.optionValueIds())
                .originalPrice(result.originalPrice())
                .discountedPrice(result.discountedPrice())
                .discountRate(result.discountRate())
                .stockQuantity(result.stockQuantity())
                .build();
    }
}
