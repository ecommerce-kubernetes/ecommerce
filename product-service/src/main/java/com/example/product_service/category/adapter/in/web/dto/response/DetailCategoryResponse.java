package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import com.example.product_service.category.application.service.dto.result.DetailCategoryResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record DetailCategoryResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,

        String name,

        Integer depth,

        boolean isLeaf,

        List<PathCategoryResponse> breadcrumb
) {

    @Builder
    public record PathCategoryResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long id,

            String name,

            int depth
    ) {

        public static PathCategoryResponse from(CategoryResult category){
            return PathCategoryResponse.builder()
                    .id(category.id())
                    .name(category.name())
                    .depth(category.depth())
                    .build();
        }

        public static List<PathCategoryResponse> from(List<CategoryResult> categories) {
            return categories.stream().map(PathCategoryResponse::from).toList();
        }
    }

    public static DetailCategoryResponse from(DetailCategoryResult detail) {
        return DetailCategoryResponse.builder()
                .id(detail.id())
                .name(detail.name())
                .depth(detail.depth())
                .isLeaf(detail.isLeaf())
                .breadcrumb(PathCategoryResponse.from(detail.breadcrumb()))
                .build();
    }
}
