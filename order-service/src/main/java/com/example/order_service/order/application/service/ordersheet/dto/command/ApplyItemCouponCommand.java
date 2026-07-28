package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

@Builder
public record ApplyItemCouponCommand(
        Long userId,
        Long orderSheetId,
        String orderSheetItemId,
        Long itemCouponId
) {
}
