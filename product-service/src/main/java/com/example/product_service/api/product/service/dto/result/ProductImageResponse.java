package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductImageResponse {
    private String imageUrl;
    private Integer order;
    private boolean isThumbnail;

    @Builder
    private ProductImageResponse(String imageUrl, Integer order, boolean isThumbnail) {
        this.imageUrl = imageUrl;
        this.order = order;
        this.isThumbnail = isThumbnail;
    }

    public static ProductImageResponse from(ProductImage productImage) {
        return ProductImageResponse.builder()
                .imageUrl(productImage.getImageUrl())
                .order(productImage.getSortOrder())
                .isThumbnail(productImage.isThumbnail())
                .build();
    }
}
