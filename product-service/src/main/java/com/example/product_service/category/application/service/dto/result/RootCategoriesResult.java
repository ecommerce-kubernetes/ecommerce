package com.example.product_service.category.application.service.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record RootCategoriesResult(
        List<CategoryResult> categories
) {
}
