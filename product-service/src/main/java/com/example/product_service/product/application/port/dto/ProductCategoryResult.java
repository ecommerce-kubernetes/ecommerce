package com.example.product_service.product.application.port.dto;

public record ProductCategoryResult(
        Long id,

        String name,

        boolean isLeaf
) {
}
