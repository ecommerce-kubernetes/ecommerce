package com.example.product_service.api.category.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCategoryRequest {
    private String name;
    @Pattern(
            regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
            message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
    )
    private String imagePath;

    @Builder
    private UpdateCategoryRequest(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    @AssertTrue(message = "수정할 값이 하나는 존재해야합니다")
    private boolean isAtLeastOneFieldPresent() {
        return name != null || imagePath != null;
    }
}
