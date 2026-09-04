package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryDetailResponse(
        Long id,
        String name,
        Integer depth,
        boolean isLeaf,
        List<BreadcrumbItemResponse> breadcrumb
) {
    public static CategoryDetailResponse from(CategoryResult.Navigation result) {
        return CategoryDetailResponse.builder()
                .id(result.current().id())
                .name(result.current().name())
                .depth(result.current().depth())
                .isLeaf(result.children().isEmpty())
                .breadcrumb(result.path().stream().map(BreadcrumbItemResponse::from).toList())
                .build();
    }
}
