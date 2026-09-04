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
    @AssertTrue(message = "{category.update.atLeastOneField}")
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
