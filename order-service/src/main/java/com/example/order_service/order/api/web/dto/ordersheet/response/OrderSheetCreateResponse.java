package com.example.order_service.order.api.web.dto.ordersheet.response;

import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderSheetCreateResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long orderSheetId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime expiresAt
) {
    public static OrderSheetCreateResponse from(OrderSheetCreateResult result) {
        return OrderSheetCreateResponse.builder()
                .orderSheetId(result.orderSheetId())
                .expiresAt(result.expiresAt())
                .build();
    }
}
