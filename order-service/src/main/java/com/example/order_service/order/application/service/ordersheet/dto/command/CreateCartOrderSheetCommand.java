package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateCartOrderSheetCommand(
        Long userId,
        List<Long> cartItemIds
) {
}
