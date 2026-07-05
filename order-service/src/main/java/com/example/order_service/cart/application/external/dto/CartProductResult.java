package com.example.order_service.cart.application.external.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;

@Builder
public record CartProductResult(
        Long productId,
        Long productVariantId,
        CartProductStatus status,
        Integer stock,
        String sku,
        String productName,
        String thumbnail,
        Money originalPrice,
        Integer discountRate,
        Money discountAmount,
        Money discountedPrice,
        List<ProductOption> options
) {
    @Builder
    public record ProductOption(
            String optionTypeName,
            String optionValueName
    ) {
    }
}
