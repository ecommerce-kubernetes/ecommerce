package com.example.product_service.api.product.service.dto.result;

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
}
