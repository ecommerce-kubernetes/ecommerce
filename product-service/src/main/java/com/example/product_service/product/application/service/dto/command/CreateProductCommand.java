package com.example.product_service.product.application.service.dto.command;

import lombok.Builder;

@Builder
public record CreateProductCommand(
        String name,
        Long categoryId,
        String description
) {
}
