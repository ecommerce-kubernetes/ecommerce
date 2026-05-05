package com.example.order_service.cart.application.dto.command;

import lombok.Builder;

import java.util.List;

public class CartCommand {

    @Builder
    public record AddItems (
            Long userId,
            List<Item> items
    ) {
        public List<Long> toProductVariantIds() {
            return items.stream().map(Item::productVariantId).toList();
        }
    }

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {}

    @Builder
    public record UpdateQuantity(
            Long userId,
            Long cartItemId,
            Integer quantity
    ) {}
}
