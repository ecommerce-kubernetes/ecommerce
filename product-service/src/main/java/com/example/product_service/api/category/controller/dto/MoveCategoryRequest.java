package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveCategoryRequest {
    private Long parentId;
    private Boolean isRoot;
    @Builder
    private MoveCategoryRequest(Long parentId, Boolean isRoot) {
        this.parentId = parentId;
        this.isRoot = isRoot;
    }

    @AssertTrue(message = "parentId 와 isRoot 를 명확히 지정해야합니다")
    private boolean isValidRequest() {
        boolean hasParentId = (parentId != null);
        boolean isToRoot = Boolean.TRUE.equals(isRoot);
        if (hasParentId && isToRoot) {
            return false;
        }

        if (!hasParentId && !isToRoot) {
            return false;
        }

        return true;
    }
}
