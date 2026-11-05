package com.example.product_service.product.domain.context;

import lombok.Builder;

@Builder
public record UpdateProductContext(
        String name,
        Long categoryId,
        String description
) {
}
