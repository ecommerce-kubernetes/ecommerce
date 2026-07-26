package com.example.order_service.cart.application.dto.param;

import lombok.Builder;

@Builder
public record UpdateCartItemContext(
        Long userId,
        Long cartItemId,
        Integer quantity,
        Integer maxLimit
) {
}
