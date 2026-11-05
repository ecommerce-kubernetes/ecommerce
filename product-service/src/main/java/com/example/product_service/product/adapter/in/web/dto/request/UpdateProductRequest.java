package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.UpdateProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateProductRequest(
        @NotBlank(message = "{product.name.notBlank}")
        String name,
        @NotNull(message = "{product.categoryId.notNull}")
        Long categoryId,
        String description
) {
    public UpdateProductCommand toCommand(Long productId) {
        return UpdateProductCommand.builder()
                .productId(productId)
                .name(name)
                .categoryId(categoryId)
                .description(description)
                .build();
    }
}
