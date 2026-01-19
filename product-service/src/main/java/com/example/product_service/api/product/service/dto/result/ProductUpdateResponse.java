package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
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

    public static ProductUpdateResponse from(Product product) {
        return ProductUpdateResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .build();
    }
}
