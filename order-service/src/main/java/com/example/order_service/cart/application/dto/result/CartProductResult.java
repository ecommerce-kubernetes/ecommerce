package com.example.order_service.cart.application.dto.result;

import lombok.Builder;

import java.util.List;

public class CartProductResult {

    @Builder
    public record Info(
            Long productId,
            Long productVariantId,
            ProductStatus status,
            String sku,
            String productName,
            Integer stock,
            Long originalPrice,
            Integer discountRate,
            Long discountAmount,
            Long discountedPrice,
            String thumbnail,
            List<Option> options
    ) {
    }

    @Builder
    public record Option(
            String optionTypeName,
            String optionValueName
    ) {
    }
}
