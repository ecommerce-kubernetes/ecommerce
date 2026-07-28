package com.example.order_service.order.application.service.order.dto.command;

import lombok.Builder;

@Builder
public record OrderCreateCommand(
        Long userId,
        Long orderSheetId
) {
}
