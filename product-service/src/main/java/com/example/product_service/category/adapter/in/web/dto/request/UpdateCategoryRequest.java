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
                message = "{category.imagePath.pattern}"
        )
        String imagePath
) {

    @JsonIgnore
    @AssertTrue(message = "이름 또는 이미지 경로 중 하나는 필수입니다.")
    public boolean isValidateEmpty() {
        return this.name != null || this.imagePath != null;
    }

    public CategoryCommand.Update toCommand(Long categoryId) {
        return CategoryCommand.Update.builder()
                .id(categoryId)
                .name(name)
                .imagePath(imagePath)
                .build();
    }
}
