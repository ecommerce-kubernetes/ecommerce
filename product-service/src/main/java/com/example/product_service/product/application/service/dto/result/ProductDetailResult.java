package com.example.product_service.product.application.service.dto.result;

import com.example.product_service.product.domain.ProductStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailResult(
        Long productId,
        String name,
        String description,
        ProductStatus status,
        CategoryInfo category,
        List<String> images,
        List<String> descriptionImages,
        Double rating,
        Long reviewCount,
        Long originalPrice,
        Integer discountRate,
        Long price,
        List<OptionGroup> options,
        List<VariantDetail> variants
) {

    @Builder
    public record CategoryInfo(
            Long id,
            String name
    ) {
    }

    @Builder
    public record OptionGroup(
            Long optionTypeId,
            String optionTypeName,
            List<OptionValueDetail> values
    ) {
    }

    @Builder
    public record OptionValueDetail(
            Long optionValueId,
            String name
    ) {
    }

    @Builder
    public record VariantDetail(
            Long variantId,
            String sku,
            List<Long> optionValueIds,
            Long originalPrice,
            Integer discountRate,
            Long price,
            Integer stockQuantity
    ) {
    }
}
