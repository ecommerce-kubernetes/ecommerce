package com.example.order_service.cart.domain.context;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateCartItemsContext(
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
