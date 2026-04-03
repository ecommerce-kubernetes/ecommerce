package com.example.product_service.api.category.controller.dto.response;

import com.example.product_service.api.category.service.dto.result.CategoryResult;
import lombok.Builder;

public class CategoryResponse {

    @Builder
    public record Detail (
            Long id,
            String name,
            Long parentId,
            Integer depth,
            String imagePath
    ) {
        public static Detail from(CategoryResult result) {
            return Detail.builder()
                    .id(result.getId())
                    .name(result.getName())
                    .parentId(result.getParentId())
                    .depth(result.getDepth())
                    .imagePath(result.getImageUrl())
                    .build();
        }
    }
}
