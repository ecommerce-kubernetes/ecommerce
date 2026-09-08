package com.example.product_service.category.application.service.dto.result;

import com.example.product_service.category.domain.Category;
import lombok.Builder;

import java.util.List;

@Builder
public record ChildCategoriesResult(
        List<CategoryResult> categories
) {
    public static ChildCategoriesResult from(List<Category> children) {
        List<CategoryResult> categories = children.stream().map(CategoryResult::from).toList();
        return ChildCategoriesResult.builder()
                .categories(categories)
                .build();
    }
}
