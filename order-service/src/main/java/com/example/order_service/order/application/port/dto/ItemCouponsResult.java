package com.example.order_service.order.application.port.dto;

import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Map<Long, ItemCouponResult> toMap() {
        return itemCoupons.stream()
                .collect(Collectors.toMap(
                        couponResult -> couponResult.itemCoupon.getItemCouponId(),
                        Function.identity()
                ));
    }
}
