package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

@Builder
public record MoveCategoryResponse(
        Long id
) {
    public static MoveCategoryResponse of(Long categoryId) {
        return new MoveCategoryResponse(categoryId);
    }
}
