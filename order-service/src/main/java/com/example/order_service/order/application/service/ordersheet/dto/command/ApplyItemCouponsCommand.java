package com.example.order_service.order.application.service.ordersheet.dto.command;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.Builder;

import java.util.List;

@Builder
public record ApplyItemCouponsCommand(
        Long userId,
        Long orderSheetId,
        List<ItemCouponCommand> itemCouponCommands
) {

    public ApplyItemCouponsCommand {
        if (itemCouponCommands != null && !itemCouponCommands.isEmpty()) {
            long uniqueItemCount = itemCouponCommands.stream()
                    .map(ItemCouponCommand::orderSheetItemId)
                    .distinct()
                    .count();

            if (uniqueItemCount != itemCouponCommands.size()) {
                throw new BusinessException(OrderErrorCode.DUPLICATE_ITEM_COUPON_REQUEST);}
        }
    }

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
