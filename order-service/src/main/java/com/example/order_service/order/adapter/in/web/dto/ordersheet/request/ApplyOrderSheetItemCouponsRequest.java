package com.example.order_service.order.adapter.in.web.dto.ordersheet.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.ApplyItemCouponsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ApplyOrderSheetItemCouponsRequest(
        @Valid
        @NotEmpty(message = "{orderSheet.applyItemCoupons.notEmpty}")
        List<ApplyItemCouponRequest> applyItemCoupons
) {

    @Builder
    public record ApplyItemCouponRequest(
            @NotNull(message = "{orderSheet.orderSheetItemId.notNull}")
            Long orderSheetItemId,
            @NotNull(message = "{orderSheet.itemCouponId.notNull}")
            Long itemCouponId
    ) {
        public ApplyItemCouponsCommand.ItemCouponCommand toCommand() {
            return ApplyItemCouponsCommand.ItemCouponCommand.builder()
                    .orderSheetItemId(orderSheetItemId)
                    .itemCouponId(itemCouponId)
                    .build();
        }
    }

    public ApplyItemCouponsCommand toCommand(Long userId, Long orderSheetId) {

        return ApplyItemCouponsCommand.builder()
                .userId(userId)
                .orderSheetId(orderSheetId)
                .itemCouponCommands(mapToCommandApplyItemCoupons())
                .build();
    }

    private List<ApplyItemCouponsCommand.ItemCouponCommand> mapToCommandApplyItemCoupons() {
        return this.applyItemCoupons.stream().map(ApplyItemCouponRequest::toCommand).toList();
    }
}
