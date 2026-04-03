package com.example.product_service.api.category.service.dto.result;

import com.example.product_service.api.category.domain.model.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CategoryResult {
    private Long id;
    private String name;
    private Long parentId;
    private Integer depth;
    private String imagePath;

    @Builder
    public CategoryResult(Long id, String name, Long parentId, Integer depth, String imagePath) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.depth = depth;
        this.imagePath = imagePath;
    }

    public static CategoryResult from(Category category) {
        return CategoryResult.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() == null ? null : category.getParent().getId())
                .depth(category.getDepth())
                .imagePath(category.getImageUrl())
                .build();
    }
}
