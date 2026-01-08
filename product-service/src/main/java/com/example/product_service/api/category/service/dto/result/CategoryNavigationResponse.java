package com.example.product_service.api.category.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryNavigationResponse {
    private CategoryResponse current;
    private List<CategoryResponse> ancestors;
    private List<CategoryResponse> siblings;
    private List<CategoryResponse> children;

    @Builder
    private CategoryNavigationResponse(CategoryResponse current,
                                       List<CategoryResponse> ancestors,
                                       List<CategoryResponse> siblings,
                                       List<CategoryResponse> children) {
        this.current = current;
        this.ancestors = ancestors;
        this.siblings = siblings;
        this.children = children;
    }

    public static CategoryNavigationResponse of(CategoryResponse current,
                                                List<CategoryResponse> ancestors,
                                                List<CategoryResponse> siblings,
                                                List<CategoryResponse> children) {
        return CategoryNavigationResponse.builder()
                .current(current)
                .ancestors(ancestors)
                .siblings(siblings)
                .children(children)
                .build();
    }
}
