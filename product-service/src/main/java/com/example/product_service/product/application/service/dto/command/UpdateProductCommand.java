package com.example.product_service.product.application.service.dto.command;

import lombok.Builder;

@Builder
public record UpdateProductCommand(
        Long productId,
        String name,
        Long categoryId,
        String description
) {
}
