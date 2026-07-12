package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.application.dto.data.CartItemData;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityResult(
        Long cartItemId,
        Long productVariantId,
        Integer quantity
) {

    public static UpdateCartItemQuantityResult from(CartItemData data) {
        return UpdateCartItemQuantityResult.builder()
                .cartItemId(data.cartItemId())
                .productVariantId(data.productVariantId())
                .quantity(data.quantity())
                .build();

    }
}
