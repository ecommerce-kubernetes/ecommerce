package com.example.product_service.api.category.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryNavigationResponse {
    private CategoryResponse current;
    private List<CategoryResponse> path;
    private List<CategoryResponse> siblings;
    private List<CategoryResponse> child;

    @Builder
    private CategoryNavigationResponse(CategoryResponse current,
                                       List<CategoryResponse> path,
                                       List<CategoryResponse> siblings,
                                       List<CategoryResponse> child) {
        this.current = current;
        this.path = path;
        this.siblings = siblings;
        this.child = child;
    }
}
