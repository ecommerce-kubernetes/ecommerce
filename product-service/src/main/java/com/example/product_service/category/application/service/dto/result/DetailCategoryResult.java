package com.example.product_service.category.application.service.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record DetailCategoryResult(
        Long id,

        String name,

        int depth,

        String path,

        String imagePath,

        boolean isLeaf,

        List<CategoryResult> breadcrumb
) {
}
