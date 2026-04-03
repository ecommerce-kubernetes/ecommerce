package com.example.product_service.api.category.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryNavigationResult {
    private CategoryResult current;
    private List<CategoryResult> path;
    private List<CategoryResult> siblings;
    private List<CategoryResult> children;

    @Builder
    private CategoryNavigationResult(CategoryResult current,
                                     List<CategoryResult> path,
                                     List<CategoryResult> siblings,
                                     List<CategoryResult> children) {
        this.current = current;
        this.path = path;
        this.siblings = siblings;
        this.children = children;
    }

    public static CategoryNavigationResult of(CategoryResult current,
                                              List<CategoryResult> path,
                                              List<CategoryResult> siblings,
                                              List<CategoryResult> children) {
        return CategoryNavigationResult.builder()
                .current(current)
                .path(path)
                .siblings(siblings)
                .children(children)
                .build();
    }
}
