package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductDescriptionImageRequest {
    @NotNull(message = "상품 설명 이미지는 필수 입니다")
    List<String> images;

}
