package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreateResponse {
    private Long productId;
    @Builder
    private ProductCreateResponse(Long productId) {
        this.productId = productId;
    }

    public static ProductCreateResponse from(Product product) {
        return ProductCreateResponse.builder().productId(product.getId()).build();
    }
}
