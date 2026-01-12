package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductImageCreateResponse {
    private Long productId;
    private List<ProductImageResponse> images;

    @Builder
    private ProductImageCreateResponse(List<ProductImageResponse> images, Long productId) {
        this.images = images;
        this.productId = productId;
    }
}
