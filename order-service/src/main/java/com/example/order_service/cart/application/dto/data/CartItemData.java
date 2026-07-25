package com.example.order_service.cart.application.dto.data;

import com.example.order_service.cart.domain.CartItem;
import lombok.Builder;

@Builder
public record CartItemData(
        Long cartItemId,
        Long productVariantId,
        Integer quantity
) {
    public static CartItemData from(CartItem cartItem) {
        return CartItemData.builder()
                .cartItemId(cartItem.getId())
                .productVariantId(cartItem.getProductVariantId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
