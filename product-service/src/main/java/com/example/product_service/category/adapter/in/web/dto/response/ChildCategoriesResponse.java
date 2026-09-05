package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import lombok.Builder;

import java.util.List;

@Builder
public record ChildCategoriesResponse(
        List<ChildCategoryResponse> categories
) {

    public record ChildCategoryResponse(
            Long id,
            String name,
            String imagePath,
            boolean isLeaf
    ) {}

    public static ChildCategoriesResponse from(List<CategoryResultDeprecated.Tree> children) {
        return null;
    }
}
