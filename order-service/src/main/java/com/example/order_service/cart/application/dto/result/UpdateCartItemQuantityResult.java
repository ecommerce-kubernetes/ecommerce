package com.example.order_service.cart.application.dto.result;

import lombok.Builder;

@Builder
public record UpdateCartItemQuantityResult(
        Long cartItemId,
        Long productVariantId,
        Integer quantity
) {
}
