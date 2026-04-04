package com.example.product_service.api.product.controller.dto;

import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class ProductRequest {

    @Builder
    public record CreateRequest(
            @NotBlank(message = "상품 이름은 필수 입니다")
            String name,
            @NotNull(message = "카테고리 id는 필수 입니다")
            Long categoryId,
            String description
    )
    {
        public ProductCreateCommand toCommand() {
            return ProductCreateCommand.builder()
                    .name(this.name())
                    .categoryId(this.categoryId())
                    .description(this.description())
                    .build();
        }
    }
}
