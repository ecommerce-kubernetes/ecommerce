package com.example.product_service.api.category.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CategoryTreeResponse {
    private Long categoryId;
    private String name;
    private Long parentId;
    private String imageUrl;
    private Integer depth;

    private List<CategoryTreeResponse> children;

    @Builder
    public CategoryTreeResponse(Long categoryId, String name, Long parentId, String imageUrl, Integer depth) {
        this.categoryId = categoryId;
        this.name = name;
        this.parentId = parentId;
        this.imageUrl = imageUrl;
        this.depth = depth;
        this.children = new ArrayList<>();
    }

    public void addChild(CategoryTreeResponse child) {
        children.add(child);
    }
}
