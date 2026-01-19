package com.example.product_service.api.category.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryNavigationResponse {
    private CategoryResponse current;
    private List<CategoryResponse> path;
    private List<CategoryResponse> siblings;
    private List<CategoryResponse> children;

    @Builder
    private CategoryNavigationResponse(CategoryResponse current,
                                       List<CategoryResponse> path,
                                       List<CategoryResponse> siblings,
                                       List<CategoryResponse> children) {
        this.current = current;
        this.path = path;
        this.siblings = siblings;
        this.children = children;
    }

    public static CategoryNavigationResponse of(CategoryResponse current,
                                                List<CategoryResponse> path,
                                                List<CategoryResponse> siblings,
                                                List<CategoryResponse> children) {
        return CategoryNavigationResponse.builder()
                .current(current)
                .path(path)
                .siblings(siblings)
                .children(children)
                .build();
    }
}
