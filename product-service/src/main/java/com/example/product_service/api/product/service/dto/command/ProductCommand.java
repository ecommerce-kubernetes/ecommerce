package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;

import java.util.List;

public class ProductCommand {

    @Builder
    public record Create (
            String name,
            Long categoryId,
            String description
    ) { }

    @Builder
    public record OptionRegister (
            Long productId,
            List<Long> optionTypeIds
    ) { }

    @Builder
    public record AddVariant (
            Long productId,
            List<VariantDetail> variants
    ) { }

    @Builder
    public record VariantDetail (
            Long originalPrice,
            Integer discountRate,
            Integer stockQuantity,
            List<Long> optionValueIds
    ) { }
}
