package com.example.order_service.order.api.web.dto.order.request;

import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderCreateRequest(
        @NotNull(message = "{order.orderSheetId.notNull}")
        Long orderSheetId
) {
        public CreateOrderCommand toCommand(Long userId) {
                return CreateOrderCommand.builder()
                        .userId(userId)
                        .orderSheetId(orderSheetId)
                        .build();
        }
}
