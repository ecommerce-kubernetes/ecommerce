package com.example.order_service.api.cart.facade.dto.command;

import lombok.Builder;

import java.util.List;

public class CartCommand {

    @Builder
    public record AddItems (
            Long userId,
            List<Item> items
    ) { }

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
