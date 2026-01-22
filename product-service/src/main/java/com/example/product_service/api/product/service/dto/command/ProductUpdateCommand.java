package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductUpdateCommand {
    private Long productId;
    private String name;
    private Long categoryId;
    private String description;

    @Builder
    private ProductUpdateCommand(Long productId, String name, Long categoryId, String description) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
    }
}
