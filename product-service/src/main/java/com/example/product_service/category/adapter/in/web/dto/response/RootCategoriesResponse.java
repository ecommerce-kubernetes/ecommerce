package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record RootCategoriesResponse(
        List<RootCategoryResponse> categories
) {
    public record RootCategoryResponse(
            Long id,
            String name,
            String imagePath,
            boolean isLeaf
    ) {}

    public static RootCategoriesResponse from(List<CategoryResult.Tree> roots) {
        return null;
    }
}
