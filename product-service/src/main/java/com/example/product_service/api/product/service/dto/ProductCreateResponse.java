package com.example.product_service.api.product.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreateResponse {
    private Long productId;
    private String name;
    private Long categoryId;
    private Integer basePrice;
    private String status;
    private String createdAt;

    @Builder
    private ProductCreateResponse(Long productId, String name, Long categoryId, Integer basePrice, String status, String createdAt) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.basePrice = basePrice;
        this.status = status;
        this.createdAt = createdAt;
    }
}
