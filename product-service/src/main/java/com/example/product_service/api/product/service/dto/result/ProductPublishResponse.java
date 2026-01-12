package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductPublishResponse {
    private Long productId;
    private String status;
    private String publishedAt;

    @Builder
    private ProductPublishResponse(Long productId, String status, String publishedAt) {
        this.productId = productId;
        this.status = status;
        this.publishedAt = publishedAt;
    }
}
