package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCategoryRequest {
    private String name;
    @URL(message = "imageUrl 형식은 URL 형식이여야합니다")
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
