package com.example.order_service.order.application.port.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderCartItemsResult(
        List<Item> items
) {

    @Builder
    public record Item(
            Long cartItemId,
            Long productVariantId,
            int quantity
    ) {
    }

    public List<Long> toProductVariantIds() {
        return items.stream().map(Item::productVariantId).toList();
    }
}
