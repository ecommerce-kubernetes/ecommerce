package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

@Builder
public record ApplyCartCouponCommand(
        Long userId,
        Long orderSheetId,
        Long cartCouponId
) {
}
