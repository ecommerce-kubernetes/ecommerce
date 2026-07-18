package com.example.order_service.order.api.web.dto.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.ApplyItemCouponCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ApplyOrderSheetItemCouponRequest(
        @NotNull(message = "{orderSheet.itemCouponId.notNull}")
        Long itemCouponId
) {
    public ApplyItemCouponCommand toCommand(Long userId, String orderSheetId, String orderSheetItemId) {
        return ApplyItemCouponCommand.builder()
                .userId(userId)
                .orderSheetId(orderSheetId)
                .orderSheetItemId(orderSheetItemId)
                .itemCouponId(itemCouponId)
                .build();
    }
}
