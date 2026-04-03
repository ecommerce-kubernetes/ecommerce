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
    private String imageUrl;

    private List<CategoryTreeResult> children;

    @Builder
    public CategoryTreeResult(Long id, String name, Long parentId, String imageUrl, Integer depth) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.imageUrl = imageUrl;
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
                .imageUrl(category.getImageUrl())
                .build();
    }

    public static List<CategoryTreeResult> convertTree(List<Category> allCategories) {
        List<CategoryTreeResult> allDtoList = allCategories.stream().map(CategoryTreeResult::from).toList();
        Map<Long, CategoryTreeResult> dtoMap = allDtoList.stream().collect(Collectors.toMap(CategoryTreeResult::getId, Function.identity()));
        List<CategoryTreeResult> rootCategories = new ArrayList<>();
        for (CategoryTreeResult category : allDtoList) {
            // depth 가 1 이면 최상위 카테고리
            if (category.getDepth() == 1) {
                rootCategories.add(category);
            } else {
                // depth 가 1 이상이면 map에서 부모 카테고리를 찾아 addChild() 메서드 호출
                CategoryTreeResult parent = dtoMap.get(category.getParentId());
                parent.addChild(category);
            }
        }
        return rootCategories;
    }
}
