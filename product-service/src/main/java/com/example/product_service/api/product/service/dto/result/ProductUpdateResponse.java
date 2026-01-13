package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductUpdateResponse {
    private Long productId;
    private String name;
    private String description;
    private Long categoryId;

    @Builder
    private ProductUpdateResponse(Long productId, String name, String description, Long categoryId) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
    }
}
