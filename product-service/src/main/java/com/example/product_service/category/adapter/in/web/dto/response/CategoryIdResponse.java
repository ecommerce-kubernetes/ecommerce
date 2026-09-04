package com.example.product_service.category.adapter.in.web.dto.response;

import com.example.product_service.category.application.service.dto.result.CategoryResult;
import lombok.Builder;

@Builder
public record CategoryIdResponse(
        Long id
) {
    public static CategoryIdResponse from(CategoryResult.Detail result) {
        return new CategoryIdResponse(result.id());
    }
}
