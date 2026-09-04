package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record TreeCategoriesResponse(
        List<TreeCategoryResponse> categories
) {
    public record TreeCategoryResponse(
            Long id,
            String name,
            Integer depth,
            boolean isLeaf,
            List<TreeCategoryResponse> children
    ) {}

    public static TreeCategoriesResponse from(List<CategoryResult.Tree> List) {
        return null;
    }
}
