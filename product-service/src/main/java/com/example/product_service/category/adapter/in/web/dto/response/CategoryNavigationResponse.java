package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryNavigationResponse(
        CategoryDetailResponse current,
        List<CategoryDetailResponse> path,
        List<CategoryDetailResponse> siblings,
        List<CategoryDetailResponse> children
) {
    public static CategoryNavigationResponse from(CategoryResult.Navigation result) {
        return CategoryNavigationResponse.builder()
                .current(CategoryDetailResponse.from(result.current()))
                .path(result.path().stream().map(CategoryDetailResponse::from).toList())
                .siblings(result.siblings().stream().map(CategoryDetailResponse::from).toList())
                .children(result.children().stream().map(CategoryDetailResponse::from).toList())
                .build();
    }
}
