package com.example.product_service.category.application.service.dto.command;

import lombok.Builder;

public class CategoryCommand {

    @Builder
    public record Create(
            String name,
            Long parentId,
            String imagePath
    ) {}

    @Builder
    public record Update(
            Long id,
            String name,
            String imagePath
    ) { }
}
