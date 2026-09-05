package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.TreeCategoriesResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
public record TreeCategoriesResponse(
        List<TreeCategoryResponse> categories
) {

    @Builder
    public record TreeCategoryResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long id,

            String name,

            Integer depth,

            boolean isLeaf,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            List<TreeCategoryResponse> children
    ) {
        public static TreeCategoryResponse from(TreeCategoriesResult.TreeCategoryResult category) {
            return TreeCategoryResponse.builder()
                    .id(category.id())
                    .name(category.name())
                    .depth(category.depth())
                    .isLeaf(category.isLeaf())
                    .children(TreeCategoryResponse.from(category.children()))
                    .build();
        }

        public static List<TreeCategoryResponse> from(List<TreeCategoriesResult.TreeCategoryResult> categories) {
            if (categories == null || categories.isEmpty()) {
                return Collections.emptyList();
            }
            return categories.stream().map(TreeCategoryResponse::from).toList();
        }
    }

    public static TreeCategoriesResponse from(TreeCategoriesResult tree) {
        return TreeCategoriesResponse.builder()
                .categories(TreeCategoryResponse.from(tree.categories()))
                .build();
    }
}
