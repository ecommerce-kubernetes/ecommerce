package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCreateRequest {
    @NotBlank(message = "상품 이름은 필수 입니다")
    private String name;
    @NotNull(message = "카테고리 id는 필수 입니다")
    private Long categoryId;
    private String description;

    @Builder
    private ProductCreateRequest(String name, Long categoryId, String description) {
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
    }
}
