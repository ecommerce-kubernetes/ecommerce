package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductVariantResponse(
        Long productId,
        List<ProductVariantDetailResponse> variants
) {
    public static AddProductVariantResponse from(ProductResult.AddVariant result) {
        List<ProductVariantDetailResponse> variants = mappingVariants(result.variants());
        return AddProductVariantResponse.builder()
                .productId(result.productId())
                .variants(variants)
                .build();
    }

    private static List<ProductVariantDetailResponse> mappingVariants(List<ProductResult.VariantDetail> variants) {
        return variants.stream().map(ProductVariantDetailResponse::from).toList();
    }
}
