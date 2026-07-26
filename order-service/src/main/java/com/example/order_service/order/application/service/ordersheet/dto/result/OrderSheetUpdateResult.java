package com.example.order_service.order.application.service.ordersheet.dto.result;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderSheetUpdateResult(
        String orderSheetId,
        LocalDateTime expiresAt
) {
    public static OrderSheetUpdateResult of(String orderSheetId, LocalDateTime expiresAt) {
        return OrderSheetUpdateResult.builder()
                .orderSheetId(orderSheetId)
                .expiresAt(expiresAt)
                .build();
    }
}
