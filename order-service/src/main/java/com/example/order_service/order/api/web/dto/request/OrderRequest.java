package com.example.order_service.order.api.web.dto.request;

import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class OrderRequest {

    @Builder
    public record Create(
            @NotNull(message = "{order.orderSheetId.notNull}")
            String orderSheetId
    ) {
        public OrderCommand.Create toCommand(Long userId) {
            return OrderCommand.Create.builder()
                    .userId(userId)
                    .orderSheetId(orderSheetId)
                    .build();
        }
    }
}
