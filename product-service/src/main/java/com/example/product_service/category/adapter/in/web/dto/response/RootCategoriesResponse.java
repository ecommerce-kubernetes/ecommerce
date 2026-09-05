package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record RootCategoriesResponse(
        List<RootCategoryResponse> categories
) {
    @Builder
    public record RootCategoryResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long id,
            String name,
            String imagePath,
            boolean isLeaf
    ) {
        public static RootCategoryResponse from(CategoryResult category) {
            return RootCategoryResponse.builder()
                    .id(category.id())
                    .name(category.name())
                    .imagePath(category.imagePath())
                    .isLeaf(category.isLeaf())
                    .build();
        }

        public static List<RootCategoryResponse> from(List<CategoryResult> categories) {
            return categories.stream().map(RootCategoryResponse::from).toList();
        }
    }

    public static RootCategoriesResponse from(RootCategoriesResult roots) {
        return RootCategoriesResponse.builder()
                .categories(RootCategoryResponse.from(roots.categories()))
                .build();
    }
}
