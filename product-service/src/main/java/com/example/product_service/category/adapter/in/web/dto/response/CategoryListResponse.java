package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListResponse(
        List<CategoryNavItemResponse> categories
) {
    public static CategoryListResponse fromRoots(List<CategoryResult.Tree> roots) {
        List<CategoryNavItemResponse> items = roots.stream().map(CategoryNavItemResponse::from).toList();
        return new CategoryListResponse(items);
    }

    public static CategoryListResponse fromChildren(CategoryResult.Tree target) {
        List<CategoryNavItemResponse> items = target.getChildren().stream().map(CategoryNavItemResponse::from).toList();
        return new CategoryListResponse(items);
    }
}
