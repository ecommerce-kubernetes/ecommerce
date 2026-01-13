package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreateCommand {
    private String name;
    private Long categoryId;
    private String description;

    @Builder
    private ProductCreateCommand(String name, Long categoryId, String description) {
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
    }
}
