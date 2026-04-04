package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreateResult {
    private Long productId;
    @Builder
    private ProductCreateResult(Long productId) {
        this.productId = productId;
    }

    public static ProductCreateResult from(Product product) {
        return ProductCreateResult.builder().productId(product.getId()).build();
    }
}
