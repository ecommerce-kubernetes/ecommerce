package com.example.order_service.order.api.web.dto.ordersheet.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.ApplyCartCouponCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ApplyOrderSheetCartCouponRequest(

        @NotNull(message = "{orderSheet.cartCouponId.notNull}")
        Long cartCouponId
) {

    public ApplyCartCouponCommand toCommand(String orderSheetId, Long userId) {
        return ApplyCartCouponCommand.builder()
                .orderSheetId(orderSheetId)
                .userId(userId)
                .cartCouponId(cartCouponId)
                .build();
    }
}
