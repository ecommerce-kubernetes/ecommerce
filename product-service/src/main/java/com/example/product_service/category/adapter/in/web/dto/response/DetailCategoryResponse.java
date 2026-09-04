package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record DetailCategoryResponse(
        Long id,
        String name,
        Integer depth,
        boolean isLeaf,
        List<PathCategoryResponse> breadcrumb
) {
    public static DetailCategoryResponse from(CategoryResult.Navigation result) {
        return null;
    }

    public record PathCategoryResponse(
            Long id,
            String name
    ) {
    }
}
