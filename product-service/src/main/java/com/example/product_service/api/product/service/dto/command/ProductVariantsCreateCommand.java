package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductVariantsCreateCommand {
    private Long productId;
    private List<VariantDetail> variants;

    @Builder
    private ProductVariantsCreateCommand(Long productId, List<VariantDetail> variants) {
        this.productId = productId;
        this.variants = variants;
    }

    @Getter
    public static class VariantDetail {
        private Long originalPrice;
        private Integer discountRate;
        private Integer stockQuantity;
        private List<Long> optionValueIds;

        @Builder
        private VariantDetail(Long originalPrice, Integer discountRate, Integer stockQuantity, List<Long> optionValueIds) {
            this.originalPrice = originalPrice;
            this.discountRate = discountRate;
            this.stockQuantity = stockQuantity;
            this.optionValueIds = optionValueIds;
        }
    }
}
