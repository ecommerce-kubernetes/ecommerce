package com.example.product_service.category.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record CreateCategoryResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id
) {
    public static CreateCategoryResponse of(Long categoryId) {
        return new CreateCategoryResponse(categoryId);
    }
}
