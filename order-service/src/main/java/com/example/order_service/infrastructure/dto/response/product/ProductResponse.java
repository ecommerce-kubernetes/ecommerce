package com.example.order_service.infrastructure.dto.response.product;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductResponse(
        List<ProductDetail> products
) {

    @Builder
    public record ProductDetail(
            Long productId,
            Long productVariantId,
            String status,
            Integer stock,
            String sku,
            String productName,
            String thumbnail,
            UnitPrice unitPrice,
            List<ProductOption> options

    ){}

    @Builder
    public record UnitPrice (
            Long originalPrice,
            Integer discountRate,
            Long discountAmount,
            Long discountedPrice
    ) {
    }

    @Builder
    public record ProductOption(
            String optionTypeName,
            String optionValueName
    ) {
    }
}
