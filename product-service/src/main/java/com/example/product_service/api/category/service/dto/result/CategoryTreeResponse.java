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

    public static CategoryTreeResponse from(Category category) {
        return CategoryTreeResponse
                .builder()
                .id(category.getId())
                .name(category.getName())
                .parentId((category.getParent() == null) ? null : category.getParent().getId())
                .depth(category.getDepth())
                .build();
    }

    public static List<CategoryTreeResponse> convertTree(List<Category> allCategories) {
        List<CategoryTreeResponse> allDtoList = allCategories.stream().map(CategoryTreeResponse::from).toList();
        Map<Long, CategoryTreeResponse> dtoMap = allDtoList.stream().collect(Collectors.toMap(CategoryTreeResponse::getId, Function.identity()));
        List<CategoryTreeResponse> rootCategories = new ArrayList<>();
        for (CategoryTreeResponse category : allDtoList) {
            // depth 가 1 이면 최상위 카테고리
            if (category.getDepth() == 1) {
                rootCategories.add(category);
            } else {
                // depth 가 1 이상이면 map에서 부모 카테고리를 찾아 addChild() 메서드 호출
                CategoryTreeResponse parent = dtoMap.get(category.getParentId());
                parent.addChild(category);
            }
        }
        return rootCategories;
    }
}
