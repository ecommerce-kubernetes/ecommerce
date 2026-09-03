package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.ProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateProductRequest(
        @NotBlank(message = "상품 이름은 필수 입니다")
        String name,
        @NotNull(message = "카테고리 id는 필수 입니다")
        Long categoryId,
        String description
) {
    public ProductCommand.Create toCommand() {
        return ProductCommand.Create.builder()
                .name(name)
                .categoryId(categoryId)
                .description(description)
                .build();
    }
}
