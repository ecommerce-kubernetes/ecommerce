package com.example.product_service.category.adapter.in.web.dto.request;

import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UpdateCategoryRequest(
        String name,
        @Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
        )
        String imagePath
) {
    @JsonIgnore
    @AssertTrue(message = "수정할 값이 하나는 존재해야합니다")
    public boolean isAtLeastOneFieldPresent() {
        return name != null || imagePath != null;
    }

    public CategoryCommand.Update toCommand(Long categoryId) {
        return CategoryCommand.Update.builder()
                .id(categoryId)
                .name(name)
                .imagePath(imagePath)
                .build();
    }
}
