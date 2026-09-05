package com.example.order_service.order.application.service.order.dto.command;

import lombok.Builder;

public class OrderCommand {

    @Builder
    public record Create(
            Long userId,
            String orderSheetId
    ) {
    }

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {
        public static OrderItem of(Long productVariantId, Integer quantity) {
            return OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }
}
