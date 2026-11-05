package com.example.product_service.category.adapter.in.web.dto.request;

import com.example.product_service.category.application.service.dto.command.UpdateCategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UpdateCategoryRequest(
        @NotBlank(message = "{category.name.notBlank}")
        String name,
        @Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "{category.imagePath.pattern}"
        )
        String imagePath
) {

    public UpdateCategoryCommand toCommand(Long categoryId) {
        return UpdateCategoryCommand.builder()
                .id(categoryId)
                .name(name)
                .imagePath(imagePath)
                .build();
    }
}
