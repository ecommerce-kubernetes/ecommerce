package com.example.order_service.order.application.port.dto;

import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ItemCouponsResult(
        Long userId,
        List<ItemCouponResult> itemCoupons
) {

    @Builder
    public record ItemCouponResult(
            OrderCouponStatus status,
            ItemCouponSnapshot itemCoupon,
            LocalDateTime expiresAt
    ) {
    }
}
