package com.example.order_service.order.api.web.dto.ordersheet.response;

import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderSheetUpdateResponse(
        Long orderSheetId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime expiresAt
) {
    public static OrderSheetUpdateResponse from(OrderSheetUpdateResult result) {
        return OrderSheetUpdateResponse.builder()
                .orderSheetId(result.orderSheetId())
                .expiresAt(result.expiresAt())
                .build();
    }
}
