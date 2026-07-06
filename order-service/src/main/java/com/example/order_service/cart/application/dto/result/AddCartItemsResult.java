package com.example.order_service.cart.application.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResult(
        List<CartItemResult> items
) {
    public record CartItemResult(
            Long cartItemId,
            Long productVariantId,
            int quantity
    ) {
    }
}
