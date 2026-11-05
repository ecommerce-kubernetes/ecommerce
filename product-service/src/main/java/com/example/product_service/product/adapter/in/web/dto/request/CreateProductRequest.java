package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.CreateProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateProductRequest(
        @NotBlank(message = "{product.name.notBlank}")
        String name,
        @NotNull(message = "{product.categoryId.notNull}")
        Long categoryId,
        String description
) {
    public CreateProductCommand toCommand() {
        return CreateProductCommand.builder()
                .name(name)
                .categoryId(categoryId)
                .description(description)
                .build();
    }
}
