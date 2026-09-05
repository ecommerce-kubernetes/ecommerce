package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record TreeCategoriesResponse(
        List<TreeCategoryResponse> categories
) {
    public record TreeCategoryResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long id,
            String name,
            Integer depth,
            boolean isLeaf,
            List<TreeCategoryResponse> children
    ) {}

    public static TreeCategoriesResponse from(List<CategoryResultDeprecated.Tree> List) {
        return null;
    }
}
