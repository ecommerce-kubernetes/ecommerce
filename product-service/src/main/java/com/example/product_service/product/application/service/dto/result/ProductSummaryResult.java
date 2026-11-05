package com.example.product_service.product.application.service.dto.result;

import lombok.Builder;

@Builder
public record ProductSummaryResult(
        Long productId,
        String name,
        String thumbnail,
        Long originalPrice,
        Integer discountRate,
        Long price,
        Long reviewCount
) {
}
