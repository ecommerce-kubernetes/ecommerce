package com.example.product_service.category.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record MoveCategoryResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id
) {
    public static MoveCategoryResponse of(Long categoryId) {
        return new MoveCategoryResponse(categoryId);
    }
}
