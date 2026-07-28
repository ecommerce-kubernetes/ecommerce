package com.example.order_service.order.api.web.dto.order.request;

import com.example.order_service.order.application.service.order.dto.command.OrderCreateCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderCreateRequest(
        @NotNull(message = "{order.orderSheetId.notNull}")
        Long orderSheetId
) {
        public OrderCreateCommand toCommand(Long userId) {
                return OrderCreateCommand.builder()
                        .userId(userId)
                        .orderSheetId(orderSheetId)
                        .build();
        }
}
