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
    private String imageUrl;

    @Builder
    public CategoryResult(Long id, String name, Long parentId, Integer depth, String imageUrl) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.depth = depth;
        this.imageUrl = imageUrl;
    }

    public static CategoryResult from(Category category) {
        return CategoryResult.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() == null ? null : category.getParent().getId())
                .depth(category.getDepth())
                .imageUrl(category.getImageUrl())
                .build();
    }
}
