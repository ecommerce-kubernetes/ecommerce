package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

@Builder
public record ApplyPointCommand(
        Long orderSheetId,
        Long userId,
        Long usedPoints
) {
}
