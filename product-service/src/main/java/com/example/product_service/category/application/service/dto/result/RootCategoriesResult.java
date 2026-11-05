package com.example.product_service.category.application.service.dto.result;

import com.example.product_service.category.domain.Category;
import lombok.Builder;

import java.util.List;

@Builder
public record RootCategoriesResult(
        List<CategoryResult> categories
) {

    public static RootCategoriesResult from(List<Category> roots) {
        List<CategoryResult> categories = roots.stream().map(CategoryResult::from).toList();
        return RootCategoriesResult.builder()
                .categories(categories)
                .build();
    }
}
