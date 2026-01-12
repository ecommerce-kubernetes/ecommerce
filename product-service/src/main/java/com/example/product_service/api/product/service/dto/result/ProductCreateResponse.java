package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreateResponse {
    private Long productId;
    private String name;
    private Long categoryId;
    private Long price;
    private String status;
    private String createdAt;

    @Builder
    private ProductCreateResponse(Long productId, String name, Long categoryId, Long price, String status, String createdAt) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }
}
