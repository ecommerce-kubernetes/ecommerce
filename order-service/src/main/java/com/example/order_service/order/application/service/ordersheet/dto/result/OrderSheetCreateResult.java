package com.example.order_service.order.application.service.ordersheet.dto.result;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderSheetCreateResult(
        String orderSheetId,
        LocalDateTime expiresAt
) {
}
