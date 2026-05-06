package com.example.order_service.cart.application.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public class CartCommand {

    @Builder
    public record AddItems (
            @NotNull
            Long userId,
            @NotEmpty @Valid
            List<Item> items
    ) {
        public List<Long> toProductVariantIds() {
            return items.stream().map(Item::productVariantId).toList();
        }
    }

    @Builder
    public record Item(
            @NotNull
            Long productVariantId,
            @Min(1)
            Integer quantity
    ) {}

    @Builder
    public record UpdateQuantity(
            Long userId,
            Long cartItemId,
            Integer quantity
    ) {}
}
