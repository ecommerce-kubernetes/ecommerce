package com.example.product_service.category.application.service.dto.command;

import lombok.Builder;

@Builder
public record UpdateCategoryCommand(
        Long id,
        String name,
        String imagePath
) {
}
