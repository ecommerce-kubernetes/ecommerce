package com.example.product_service.category.application.service.dto.result;

import com.example.product_service.category.domain.Category;
import lombok.Builder;

@Builder
public record CategoryResult(
        Long id,

        String name,

        int depth,

        String imagePath,

        boolean isLeaf
) {

    public static CategoryResult from(Category category) {
        return CategoryResult.builder()
                .id(category.getId())
                .name(category.getName())
                .depth(category.getDepth())
                .imagePath(category.getImagePath())
                .isLeaf(category.isLeaf())
                .build();
    }
}
