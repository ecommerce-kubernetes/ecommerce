package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreateResponse {
    private Long productId;
    @Builder
    private ProductCreateResponse(Long productId) {
        this.productId = productId;
    }
}
