package com.example.product_service.api.product.controller.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImageCreateRequest {
    List<String> images;

    @Builder
    private ProductImageCreateRequest(List<String> images) {
        this.images = images;
    }
}
