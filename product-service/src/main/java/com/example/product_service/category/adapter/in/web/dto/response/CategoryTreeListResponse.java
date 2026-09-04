package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryTreeListResponse(
        List<CategoryTreeResponse> categories
) {
    public static CategoryTreeListResponse from(List<CategoryResult.Tree> results) {
        return new CategoryTreeListResponse(CategoryTreeResponse.from(results));
    }
}
