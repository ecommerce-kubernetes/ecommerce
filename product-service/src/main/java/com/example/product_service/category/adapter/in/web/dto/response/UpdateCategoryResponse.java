package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

@Builder
public record UpdateCategoryResponse(
        Long id
) {

    public static UpdateCategoryResponse from(CategoryResult.Detail result) {
        return new UpdateCategoryResponse(result.id());
    }
}
