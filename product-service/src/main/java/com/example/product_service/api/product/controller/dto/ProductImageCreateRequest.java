package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImageCreateRequest {
    @NotNull(message = "상품 이미지는 필수 입니다")
    List<String> images;

    @Builder
    private ProductImageCreateRequest(List<String> images) {
        this.images = images;
    }
}
