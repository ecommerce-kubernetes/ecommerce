package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

@Builder
public record CreateProductResponse(
        Long productId
) {
    public static CreateProductResponse from(ProductResult.Create result) {
        return CreateProductResponse.builder()
                .productId(result.productId())
                .build();
    }
}
