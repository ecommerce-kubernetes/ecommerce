package com.example.order_service.order.api.dto.request;

import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class OrderRequest {

    @Builder
    public record Create(
            @NotNull(message = "주문서 ID는 필수 입니다")
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
