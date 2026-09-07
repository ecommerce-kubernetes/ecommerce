package com.example.product_service.product.application.service.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record AddProductVariantCommand(
        Long productId,
        List<VariantDetail> variants
) {

    @Builder
    public record VariantDetail(
            Long originalPrice,
            Integer discountRate,
            Integer stockQuantity,
            List<Long> optionValueIds
    ) {
    }
}
