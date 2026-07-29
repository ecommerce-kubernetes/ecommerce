package com.example.order_service.cart.domain.context;

import lombok.Builder;

@Builder
public record UpdateCartItemContext(
        Long userId,
        Long cartItemId,
        Integer quantity,
        Integer maxLimit
) {
}
