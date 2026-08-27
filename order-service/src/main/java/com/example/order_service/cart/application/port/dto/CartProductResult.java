package com.example.order_service.cart.application.port.dto;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public record CartProductResult(
        List<CartProductDetail> products
) {
    public Map<Long, CartProductDetail> toMap() {
        return products.stream().collect(Collectors.toMap(CartProductDetail::productVariantId, Function.identity()));
    }

    @Builder
    public record CartProductDetail(
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
    }

    @Builder
    public record ProductOption(
            String optionTypeName,
            String optionValueName
    ) {
    }
}
