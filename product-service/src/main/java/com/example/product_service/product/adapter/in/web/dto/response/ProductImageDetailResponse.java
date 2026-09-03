package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

@Builder
public record ProductImageDetailResponse(
        Long imageId,
        String imagePath,
        Boolean isThumbnail,
        Integer sortOrder
) {
    public static ProductImageDetailResponse from(ProductResult.ImageDetail image) {
        return ProductImageDetailResponse.builder()
                .imageId(image.imageId())
                .imagePath(image.imagePath())
                .isThumbnail(image.isThumbnail())
                .sortOrder(image.sortOrder())
                .build();
    }
}
