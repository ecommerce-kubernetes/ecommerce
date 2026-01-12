package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductImageResponse {
    private Long productImageId;
    private String imageUrl;
    private Integer order;
    private boolean isThumbnail;

    @Builder
    private ProductImageResponse(Long productImageId, String imageUrl, Integer order, boolean isThumbnail) {
        this.productImageId = productImageId;
        this.imageUrl = imageUrl;
        this.order = order;
        this.isThumbnail = isThumbnail;
    }
}
