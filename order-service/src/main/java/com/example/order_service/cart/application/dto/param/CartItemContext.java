package com.example.order_service.cart.application.dto.param;

import lombok.Builder;

import java.util.List;

@Builder
public record CartItemContext(
        Long userId,
        List<Item> items
) {

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity,
            Integer maxLimit
    ) {
    }
}
