package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record ApplyItemCouponsCommand(
        Long userId,
        Long orderSheetId,
        List<ItemCouponCommand> itemCouponCommands
) {

    @Builder
    public record ItemCouponCommand(
            Long orderSheetItemId,
            Long itemCouponId
    ) {
    }

    public List<Long> toItemCouponIds() {
        return itemCouponCommands.stream().map(item -> item.itemCouponId).toList();
    }
}
