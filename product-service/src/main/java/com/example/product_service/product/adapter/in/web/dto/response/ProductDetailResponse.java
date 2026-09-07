package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductDetailResult;
import com.example.product_service.product.domain.model.ProductStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailResponse(
        Long productId,
        String name,
        String description,
        ProductStatus status,
        CategoryResponse category,
        List<String> images,
        List<String> descriptionImages,
        Double rating,
        Long reviewCount,
        Long originalPrice,
        Integer discountRate,
        Long price,
        List<ProductOptionResponse> options,
        List<ProductVariantResponse> variants
) {

    @Builder
    public record CategoryResponse(
            Long id,
            String name
    ) {
        public static CategoryResponse from(ProductDetailResult.CategoryInfo category) {
            return CategoryResponse.builder()
                    .id(category.id())
                    .name(category.name())
                    .build();
        }
    }

    @Builder
    public record ProductOptionResponse(
            Long optionTypeId,
            String optionTypeName,
            List<ProductOptionValueResponse> values
    ) {
        public static ProductOptionResponse from(ProductDetailResult.OptionGroup group) {
            return ProductOptionResponse.builder()
                    .optionTypeId(group.optionTypeId())
                    .optionTypeName(group.optionTypeName())
                    .values(group.values().stream().map(ProductOptionValueResponse::from).toList())
                    .build();
        }
    }

    @Builder
    public record ProductOptionValueResponse(
            Long optionValueId,
            String name
    ) {
        public static ProductOptionValueResponse from(ProductDetailResult.OptionValueDetail value) {
            return ProductOptionValueResponse.builder()
                    .optionValueId(value.optionValueId())
                    .name(value.name())
                    .build();
        }
    }

    @Builder
    public record ProductVariantResponse(
            Long variantId,
            String sku,
            List<Long> optionValueIds,
            Long originalPrice,
            Integer discountRate,
            Long price,
            Integer stockQuantity
    ) {
        public static ProductVariantResponse from(ProductDetailResult.VariantDetail variant) {
            return ProductVariantResponse.builder()
                    .variantId(variant.variantId())
                    .sku(variant.sku())
                    .optionValueIds(variant.optionValueIds())
                    .originalPrice(variant.originalPrice())
                    .discountRate(variant.discountRate())
                    .price(variant.price())
                    .stockQuantity(variant.stockQuantity())
                    .build();
        }
    }

    public static ProductDetailResponse from(ProductDetailResult result) {
        return ProductDetailResponse.builder()
                .productId(result.productId())
                .name(result.name())
                .description(result.description())
                .status(result.status())
                .category(CategoryResponse.from(result.category()))
                .images(result.images())
                .descriptionImages(result.descriptionImages())
                .rating(result.rating())
                .reviewCount(result.reviewCount())
                .originalPrice(result.originalPrice())
                .discountRate(result.discountRate())
                .price(result.price())
                .options(result.options().stream().map(ProductOptionResponse::from).toList())
                .variants(result.variants().stream().map(ProductVariantResponse::from).toList())
                .build();
    }
}
