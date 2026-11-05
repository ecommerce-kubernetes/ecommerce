package com.example.product_service.category.application.service.dto.command;

import lombok.Builder;

@Builder
public record CreateCategoryCommand(
        String name,
        Long parentId,
        String imagePath
) {
}
