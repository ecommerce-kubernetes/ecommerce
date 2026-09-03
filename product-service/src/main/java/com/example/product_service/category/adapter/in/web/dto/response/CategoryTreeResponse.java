package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryTreeResponse(
        Long id,
        String name,
        Long parentId,
        Integer depth,
        String imagePath,
        List<CategoryTreeResponse> children
) {
    public static CategoryTreeResponse from(CategoryResult.Tree result) {
        return CategoryTreeResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .parentId(result.getParentId())
                .depth(result.getDepth())
                .imagePath(result.getImagePath())
                .children(result.getChildren().stream().map(CategoryTreeResponse::from).toList())
                .build();
    }

    public static List<CategoryTreeResponse> from(List<CategoryResult.Tree> results) {
        return results.stream().map(CategoryTreeResponse::from).toList();
    }
}
