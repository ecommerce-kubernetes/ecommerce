package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

@Builder
public record BreadcrumbItemResponse(
        Long id,
        String name
) {
    public static BreadcrumbItemResponse from(CategoryResult.Detail detail) {
        return new BreadcrumbItemResponse(detail.id(), detail.name());
    }
}
