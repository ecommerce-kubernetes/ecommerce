package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ProductVariantsCreateCommand {
    private Long productId;
    @Builder.Default
    private List<VariantDetail> variants = new ArrayList<>();

    @Getter
    @Builder
    public static class VariantDetail {
        private Long originalPrice;
        private Integer discountRate;
        private Integer stockQuantity;
        @Builder.Default
        private List<Long> optionValueIds = new ArrayList<>();

    }
}
