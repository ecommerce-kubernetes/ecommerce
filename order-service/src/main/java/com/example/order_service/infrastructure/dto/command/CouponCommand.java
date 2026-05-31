package com.example.order_service.infrastructure.dto.command;

import lombok.Builder;

import java.util.List;

public class CouponCommand {

    @Builder
    public record Calculate(
            Long userId,
            Long cartCouponId,
            List<Item> items
    ) {}

    @Builder
    public record Item(
            Long productVariantId,
            Long price,
            Integer quantity,
            Long itemCouponId
    ) {}
}
