package com.example.product_service.category.application.service.dto.result;

import com.example.product_service.category.domain.Category;
import lombok.Builder;

import java.util.Comparator;
import java.util.List;

@Builder
public record DetailCategoryResult(
        Long id,

        String name,

        int depth,

        String imagePath,

        boolean isLeaf,

        List<CategoryResult> breadcrumb
) {

    public static DetailCategoryResult from(Category category, List<Category> pathCategories) {
        List<CategoryResult> breadcrumb = mapToBreadcrumb(pathCategories);
        return DetailCategoryResult.builder()
                .id(category.getId())
                .name(category.getName())
                .depth(category.getDepth())
                .imagePath(category.getImagePath())
                .isLeaf(category.isLeaf())
                .breadcrumb(breadcrumb)
                .build();
    }

    private static List<CategoryResult> mapToBreadcrumb(List<Category> pathCategories) {
        return pathCategories.stream()
                .sorted(Comparator.comparingInt(Category::getDepth))
                .map(CategoryResult::from)
                .toList();
    }
}
