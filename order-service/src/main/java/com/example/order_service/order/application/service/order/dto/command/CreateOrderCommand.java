package com.example.order_service.order.application.service.order.dto.command;

import lombok.Builder;

@Builder
public record CreateOrderCommand(
        Long userId,
        Long orderSheetId
) {
}
