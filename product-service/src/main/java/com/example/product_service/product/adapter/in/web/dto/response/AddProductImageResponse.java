package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductImageResponse(
        Long productId,
        List<ProductImageDetailResponse> images
) {
    public static AddProductImageResponse from(ProductResult.AddImage result) {
        List<ProductImageDetailResponse> images = mappingImageResponse(result.images());
        return AddProductImageResponse.builder()
                .productId(result.productId())
                .images(images)
                .build();
    }

    public static List<ProductImageDetailResponse> mappingImageResponse(List<ProductResult.ImageDetail> images) {
        return images.stream().map(ProductImageDetailResponse::from).toList();
    }
}
