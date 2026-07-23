package com.example.order_service.infrastructure.dto.response.product;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductResponse(
        List<ProductDetail> products
) {

    public record ProductDetail(
            Long productId,
            Long productVariantId,
            ProductStatus status,
            Integer stock,
            String sku,
            String productName,
            String thumbnail,
            UnitPrice unitPrice,
            List<ProductOption> options

    ){}

    public enum ProductStatus {
        PREPARING, ON_SALE, STOP_SALE, DELETED
    }

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
