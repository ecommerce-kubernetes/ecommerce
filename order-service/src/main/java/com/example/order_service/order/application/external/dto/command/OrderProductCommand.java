package com.example.order_service.order.application.external.dto.command;

import lombok.Builder;

public class OrderProductCommand {

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {}
}
