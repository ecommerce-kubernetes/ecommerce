package com.example.order_service.order.api.web.dto.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.ApplyPointCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ApplyOrderSheetPointRequest(
        @NotNull(message = "{orderSheet.usedPoints.notNull}")
        @Min(value = 0, message = "{orderSheet.usedPoints.min}")
        Long usedPoints
) {
    public ApplyPointCommand toCommand(String orderSheetId, Long userId) {
        return ApplyPointCommand.builder()
                .orderSheetId(orderSheetId)
                .userId(userId)
                .usedPoints(usedPoints)
                .build();
    }
}
