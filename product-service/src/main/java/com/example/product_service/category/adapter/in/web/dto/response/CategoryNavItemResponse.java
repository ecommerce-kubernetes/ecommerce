package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

@Builder
public record CategoryNavItemResponse(
        Long id,
        String name,
        String imagePath,
        boolean isLeaf
) {
    public static CategoryNavItemResponse from(CategoryResult.Tree tree) {
        return CategoryNavItemResponse.builder()
                .id(tree.getId())
                .name(tree.getName())
                .imagePath(tree.getImagePath())
                .isLeaf(tree.getChildren().isEmpty())
                .build();
    }
}
