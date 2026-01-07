package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveCategoryRequest {
    @NotNull(message = "parentId는 필수입니다")
    private Long parentId;

    @Builder
    private MoveCategoryRequest(Long parentId) {
        this.parentId = parentId;
    }
}
