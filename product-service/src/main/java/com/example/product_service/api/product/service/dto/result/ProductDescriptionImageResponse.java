package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductDescriptionImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDescriptionImageResponse {
    private String imageUrl;
    private Integer sortOrder;

    public static ProductDescriptionImageResponse from(ProductDescriptionImage productDescriptionImage) {
        return ProductDescriptionImageResponse.builder()
                .imageUrl(productDescriptionImage.getImageUrl())
                .sortOrder(productDescriptionImage.getSortOrder())
                .build();
    }
}
