package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record AddProductDescriptionImageResponse(
        Long productId,
        List<ProductDescriptionImageDetailResponse> descriptionImages
) {
    public static AddProductDescriptionImageResponse from(ProductResult.AddDescriptionImage result) {
        List<ProductDescriptionImageDetailResponse> images = mappingDescriptionImages(result.images());
        return AddProductDescriptionImageResponse.builder()
                .productId(result.productId())
                .descriptionImages(images)
                .build();
    }

    private static List<ProductDescriptionImageDetailResponse> mappingDescriptionImages(List<ProductResult.DescriptionImageDetail> images) {
        return images.stream().map(ProductDescriptionImageDetailResponse::from).toList();
    }
}
