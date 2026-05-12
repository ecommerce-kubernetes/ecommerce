package com.example.order_service.infrastructure.dto.command;

import lombok.Builder;

import java.util.List;

public class CouponCommand {

    @Builder
    public record CouponEvaluate(
            Long userId,
            Long totalAmount,
            List<Item> items
    ) {}

    @Builder
    public record Item(
            String itemId,
            Long productId,
            Long productVariantId,
            Long price,
            Integer quantity
    ) {}
}
