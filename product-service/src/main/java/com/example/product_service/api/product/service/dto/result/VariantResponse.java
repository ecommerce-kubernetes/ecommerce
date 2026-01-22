package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductVariant;
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

    public static VariantResponse from(ProductVariant productVariant) {
        return VariantResponse.builder()
                .variantId(productVariant.getId())
                .sku(productVariant.getSku())
                .optionValueIds(productVariant.getProductVariantOptions().stream().map(pvo -> pvo.getOptionValue().getId()).toList())
                .originalPrice(productVariant.getOriginalPrice())
                .discountedPrice(productVariant.getPrice())
                .discountRate(productVariant.getDiscountRate())
                .stockQuantity(productVariant.getStockQuantity())
                .build();
    }
}
