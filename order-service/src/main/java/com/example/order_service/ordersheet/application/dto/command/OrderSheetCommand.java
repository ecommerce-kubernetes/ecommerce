package com.example.order_service.ordersheet.application.dto.command;

import lombok.Builder;

import java.util.List;

public class OrderSheetCommand {

    @Builder
    public record Create(
            Long userId,
            List<OrderItem> items
    ) {
    }

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {
    }
}
