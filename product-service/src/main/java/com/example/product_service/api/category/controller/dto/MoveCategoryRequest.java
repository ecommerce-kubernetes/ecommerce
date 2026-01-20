package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MoveCategoryRequest {
    private Long parentId;
    @Builder
    private MoveCategoryRequest(Long parentId) {
        this.parentId = parentId;
    }
}
