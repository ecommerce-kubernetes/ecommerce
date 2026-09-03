package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

@Builder
public record CategoryDetailResponse(
        Long id,
        String name,
        Long parentId,
        Integer depth,
        String imagePath
) {
    public static CategoryDetailResponse from(CategoryResult.Detail result) {
        return CategoryDetailResponse.builder()
                .id(result.id())
                .name(result.name())
                .parentId(result.parentId())
                .depth(result.depth())
                .imagePath(result.imagePath())
                .build();
    }
}
