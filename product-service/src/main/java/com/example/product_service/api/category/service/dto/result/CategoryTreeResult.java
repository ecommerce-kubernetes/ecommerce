package com.example.product_service.api.category.service.dto.result;

import com.example.product_service.api.category.domain.model.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class CategoryTreeResult {
    private Long id;
    private String name;
    private Long parentId;
    private Integer depth;
    private String imagePath;

    private List<CategoryTreeResult> children;

    @Builder
    public CategoryTreeResult(Long id, String name, Long parentId, String imagePath, Integer depth) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.imagePath = imagePath;
        this.depth = depth;
        this.children = new ArrayList<>();
    }

    public void addChild(CategoryTreeResult child) {
        children.add(child);
    }

    public static CategoryTreeResult from(Category category) {
        return CategoryTreeResult
                .builder()
                .id(category.getId())
                .name(category.getName())
                .parentId((category.getParent() == null) ? null : category.getParent().getId())
                .depth(category.getDepth())
                .imagePath(category.getImageUrl())
                .build();
    }
}
