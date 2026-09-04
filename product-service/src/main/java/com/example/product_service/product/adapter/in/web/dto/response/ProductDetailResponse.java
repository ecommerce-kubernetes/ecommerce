package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import com.example.product_service.product.domain.model.ProductStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailResponse(
        Long productId,
        String name,
        ProductStatus status,
        Long categoryId,
        String description,
        Long displayPrice,
        Long originalPrice,
        Integer maxDiscountRate,
        Double rating,
        Long reviewCount,
        Double popularityScore,
        List<ProductOptionGroupResponse> optionGroups,
        List<ProductImageDetailResponse> images,
        List<ProductDescriptionImageDetailResponse> descriptionImages,
        List<ProductVariantDetailResponse> variants
) {
    public static ProductDetailResponse from(ProductResult.Detail result) {
        return ProductDetailResponse.builder()
                .productId(result.productId())
                .name(result.name())
                .status(result.status())
                .categoryId(result.categoryId())
                .description(result.description())
                .displayPrice(result.displayPrice())
                .originalPrice(result.originalPrice())
                .maxDiscountRate(result.maxDiscountRate())
                .rating(result.rating())
                .reviewCount(result.reviewCount())
                .popularityScore(result.popularityScore())
                .optionGroups(result.optionGroups().stream().map(ProductOptionGroupResponse::from).toList())
                .images(result.images().stream().map(ProductImageDetailResponse::from).toList())
                .descriptionImages(result.descriptionImages().stream().map(ProductDescriptionImageDetailResponse::from).toList())
                .variants(result.variants().stream().map(ProductVariantDetailResponse::from).toList())
                .build();
    }
}
