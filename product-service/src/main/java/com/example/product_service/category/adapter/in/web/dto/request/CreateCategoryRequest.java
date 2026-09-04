package com.example.product_service.category.adapter.in.web.dto.request;

import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CreateCategoryRequest(
        @NotBlank(message = "{category.name.notBlank}")
        String name,
        Long parentId,
        @Pattern(
                regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                message = "{category.imagePath.pattern}"
        )
        String imagePath
) {
    public CategoryCommand.Create toCommand() {
        return CategoryCommand.Create.builder()
                .name(name)
                .parentId(parentId)
                .imagePath(imagePath)
                .build();
    }
}
