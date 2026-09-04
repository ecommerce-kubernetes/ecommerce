package com.example.product_service.category.adapter.in.web.dto.response;

import lombok.Builder;

@Builder
public record MoveCategoryResponse(
        Long id
) {
    public static MoveCategoryResponse of(Long categoryId) {
        return new MoveCategoryResponse(categoryId);
    }
}
