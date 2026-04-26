package com.example.order_service.ordersheet.api.dto.response;

import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

public class OrderSheetResponse {

    @Builder
    public record Create (
            String sheetId,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
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
