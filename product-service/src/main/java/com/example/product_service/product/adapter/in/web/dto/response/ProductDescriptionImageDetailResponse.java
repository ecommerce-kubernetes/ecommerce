package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

@Builder
public record ProductDescriptionImageDetailResponse(
        Long imageId,
        String imagePath,
        Integer sortOrder
) {
    public static ProductDescriptionImageDetailResponse from(ProductResult.DescriptionImageDetail image) {
        return ProductDescriptionImageDetailResponse.builder()
                .imageId(image.imageId())
                .imagePath(image.imagePath())
                .sortOrder(image.sortOrder())
                .build();
    }
}
