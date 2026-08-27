package com.example.order_service.order.application.service.ordersheet.dto.result;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderSheetUpdateResult(
        Long orderSheetId,
        LocalDateTime expiresAt
) {
    public static OrderSheetUpdateResult of(Long orderSheetId, LocalDateTime expiresAt) {
        return OrderSheetUpdateResult.builder()
                .orderSheetId(orderSheetId)
                .expiresAt(expiresAt)
                .build();
    }
}
