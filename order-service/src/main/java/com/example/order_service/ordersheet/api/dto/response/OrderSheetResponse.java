package com.example.order_service.ordersheet.api.dto.response;

import com.example.order_service.ordersheet.service.dto.result.OrderSheetResult;
import lombok.Builder;

import java.time.LocalDateTime;

public class OrderSheetResponse {

    @Builder
    public record Create (
            String sheetId,
            LocalDateTime expiresAt
    ) {
        public static Create from(OrderSheetResult.Default result) {
            return Create.builder()
                    .sheetId(result.sheetId())
                    .expiresAt(result.expiresAt())
                    .build();
        }
    }
}
