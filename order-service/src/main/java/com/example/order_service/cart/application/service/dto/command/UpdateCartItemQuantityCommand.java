package com.example.order_service.cart.application.service.dto.command;

import lombok.Builder;

@Builder
public record UpdateCartItemQuantityCommand(
        Long userId,
        Long cartItemId,
        Integer quantity
) {
}
