package com.example.order_service.infrastructure.dto.response.coupon;

import lombok.Builder;

@Builder
public record ItemCouponResponse(
        Long userId,
        Long itemCouponId,
        String name,
        Integer applyQuantityLimit,
        String discountType,
        Long discountAmount,
        Integer discountRate,
        Long maxDiscountAmount
) {
}
