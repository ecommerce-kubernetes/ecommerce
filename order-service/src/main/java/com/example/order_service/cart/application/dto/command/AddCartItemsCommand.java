package com.example.order_service.cart.application.dto.command;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record AddCartItemsCommand(
        Long userId,
        List<Item> items
) {

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {
    }

    public List<Long> toProductVariantIds() {
        return items.stream().map(Item::productVariantId).toList();
    }
}
