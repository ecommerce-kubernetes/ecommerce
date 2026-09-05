package com.example.product_service.category.application.service.dto.result;

import lombok.Builder;

@Builder
public record CategoryResult(
        Long id,

        String name,

        int depth,

        String path,

        String imagePath,

        boolean isLeaf
) {
}
