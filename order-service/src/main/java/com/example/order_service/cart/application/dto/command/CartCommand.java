package com.example.order_service.cart.application.dto.command;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartCommand {

    @Builder
    public record AddItems (
            Long userId,
            List<Item> items
    ) {
        public List<Long> toProductVariantIds() {
            return items.stream().map(Item::productVariantId).toList();
        }

        public Map<Long, Integer> toQuantityMap() {
            return items.stream().collect(Collectors.toMap(Item::productVariantId, Item::quantity));
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
