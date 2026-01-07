package com.example.product_service.api.category.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer depth;
    private String imageUrl;

    private List<CategoryTreeResponse> children;

    @Builder
    public CategoryTreeResponse(Long id, String name, Long parentId, String imageUrl, Integer depth) {
        this.id = id;
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
