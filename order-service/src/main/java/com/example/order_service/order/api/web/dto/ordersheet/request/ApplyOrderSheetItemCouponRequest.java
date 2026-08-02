package com.example.order_service.order.api.web.dto.ordersheet.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.ApplyItemCouponsCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ApplyOrderSheetItemCouponRequest(
        @NotNull(message = "{orderSheet.itemCouponId.notNull}")
        Long itemCouponId
) {
    public ApplyItemCouponsCommand toCommand(Long userId, Long orderSheetId, Long orderSheetItemId) {
        ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand = ApplyItemCouponsCommand.ItemCouponCommand.builder()
                .orderSheetItemId(orderSheetItemId)
                .itemCouponId(itemCouponId)
                .build();

        return ApplyItemCouponsCommand.builder()
                .userId(userId)
                .orderSheetId(orderSheetId)
                .itemCouponCommands(List.of(itemCouponCommand))
                .build();
    }
}
