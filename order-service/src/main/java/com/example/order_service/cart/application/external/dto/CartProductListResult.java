package com.example.order_service.cart.application.external.dto;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record CartProductListResult(
        List<CartProductResult> products
) {
    public Map<Long, CartProductResult> toMap() {
        return products.stream().collect(Collectors.toMap(CartProductResult::productVariantId, Function.identity()));
    }
}
