package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.application.dto.data.CartItemData;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityResult(
        Long cartItemId
) {

    public static UpdateCartItemQuantityResult from(CartItemData data) {
        return UpdateCartItemQuantityResult.builder()
                .cartItemId(data.cartItemId())
                .build();

    }
}
