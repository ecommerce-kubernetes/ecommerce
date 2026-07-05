package com.example.order_service.cart.application.dto.data;

import lombok.Builder;

@Builder
public record CartItemData(
        Long cartItemId,
        Long productVariantId,
        Integer quantity
) {
}
