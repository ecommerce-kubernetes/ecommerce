package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record ChildCategoriesResponse(
        List<ChildCategoryResponse> categories
) {

    @Builder
    public record ChildCategoryResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long id,

            String name,

            String imagePath,

            boolean isLeaf
    ) {
        public static ChildCategoryResponse from(CategoryResult category) {
            return ChildCategoryResponse.builder()
                    .id(category.id())
                    .name(category.name())
                    .imagePath(category.imagePath())
                    .isLeaf(category.isLeaf())
                    .build();
        }

        public static List<ChildCategoryResponse> from(List<CategoryResult> categories){
            return categories.stream().map(ChildCategoryResponse::from).toList();
        }
    }

    public static ChildCategoriesResponse from(ChildCategoriesResult children) {
        return ChildCategoriesResponse.builder()
                .categories(ChildCategoryResponse.from(children.categories()))
                .build();
    }
}
