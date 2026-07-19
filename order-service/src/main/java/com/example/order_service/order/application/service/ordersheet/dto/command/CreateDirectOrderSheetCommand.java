package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateDirectOrderSheetCommand(
        Long userId,
        List<OrderVariant> items
) {
    @Builder
    public record OrderVariant (
            Long productVariantId,
            Integer quantity
    ) {}
}
