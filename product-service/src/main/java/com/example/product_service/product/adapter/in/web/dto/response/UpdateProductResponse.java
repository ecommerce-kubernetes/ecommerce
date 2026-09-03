package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

@Builder
public record UpdateProductResponse(
        Long productId,
        String name,
        String description,
        Long categoryId
) {
    public static UpdateProductResponse from(ProductResult.Update result) {
        return UpdateProductResponse.builder()
                .productId(result.productId())
                .name(result.name())
                .description(result.description())
                .categoryId(result.categoryId())
                .build();
    }
}
