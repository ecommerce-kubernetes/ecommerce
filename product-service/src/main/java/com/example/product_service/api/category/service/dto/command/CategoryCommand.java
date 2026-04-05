package com.example.product_service.api.category.service.dto.command;

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
