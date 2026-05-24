package com.example.order_service.order.application.dto.command;

import lombok.Builder;

public class OrderCommand {

    @Builder
    public record Create (
            Long userId,
            String orderSheetId
    ) {
    }
}
