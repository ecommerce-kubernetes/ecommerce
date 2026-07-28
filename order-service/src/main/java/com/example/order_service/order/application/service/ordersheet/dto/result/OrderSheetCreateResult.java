package com.example.order_service.order.application.service.ordersheet.dto.result;

import com.example.order_service.order.domain.model.OrderSheet;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderSheetCreateResult(
        Long orderSheetId,
        LocalDateTime expiresAt
) {
    public static OrderSheetCreateResult from(OrderSheet orderSheet) {
        return OrderSheetCreateResult.builder()
                .orderSheetId(orderSheet.getId())
                .expiresAt(orderSheet.getExpiresAt())
                .build();
    }
}
