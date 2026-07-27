package com.example.order_service.order.api.web.dto.order.request;

import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record OrderCreateRequest(
        @NotBlank(message = "{order.orderSheetId.notNull}")
        String orderSheetId
) {
        public OrderCommand.Create toCommand(Long userId) {
                return OrderCommand.Create.builder()
                        .userId(userId)
                        .orderSheetId(orderSheetId)
                        .build();
        }
}
