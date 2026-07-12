package com.example.order_service.cart.application.dto.command;

import lombok.Builder;

@Builder
public record UpdateCartItemQuantityCommand(
        Long userId,
        Long cartItemId,
        Integer quantity
) {
}
