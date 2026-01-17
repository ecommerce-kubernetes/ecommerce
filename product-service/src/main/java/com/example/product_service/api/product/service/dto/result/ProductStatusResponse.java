package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductStatusResponse {
    private Long productId;
    private String status;
    private String changedAt;

    @Builder
    private ProductStatusResponse(Long productId, String status, String changedAt) {
        this.productId = productId;
        this.status = status;
        this.changedAt = changedAt;
    }

    public static ProductStatusResponse publish(Product product) {
        return ProductStatusResponse.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .changedAt(product.getPublishedAt().toString())
                .build();
    }
}
