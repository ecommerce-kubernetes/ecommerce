package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCategoryRequest {
    private String name;
    private String imageUrl;

    @Builder
    private UpdateCategoryRequest(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    @AssertTrue(message = "수정할 값이 하나는 존재해야합니다")
    private boolean isAtLeastOneFieldPresent() {
        return name != null || imageUrl != null;
    }
}
