package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

import java.util.List;

public class ProductClientResponse {

    @Builder
    public record ProductDeprecated(
            Long productId,
            Long productVariantId,
            String status,
            String sku,
            String productName,
            String thumbnail,
            UnitPrice unitPrice,
            Integer stockQuantity,
            List<ProductOption> itemOptions
    ) {
    }

    @Builder
    public record Product(
            Long productId,
            Long productVariantId,
            String sku,
            String productName,
            String thumbnail,
            UnitPrice unitPrice,
            List<ProductOption> options
    ) {
    }

    @Builder
    public record UnitPrice(
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
