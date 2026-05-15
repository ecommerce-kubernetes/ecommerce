package com.example.order_service.infrastructure.dto.command;

import lombok.Builder;

import java.util.List;

public class ProductCommand {

    @Builder
    public record Validate(
            List<Item> items
    ) {
        public static Validate of(List<Item> items) {
            return Validate.builder()
                    .items(items)
                    .build();
        }
    }

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {
        public static Item of(Long productVariantId, Integer quantity) {
            return Item.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }
}
