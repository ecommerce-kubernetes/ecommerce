package com.example.order_service.infrastructure.dto.response.coupon;

import lombok.Builder;

@Builder
public record
CartCouponResponse(
        Long userId,
        Long cartCouponId,
        String name,
        Long minimumPaymentAmount,
        DiscountType discountType,
        Long discountAmount,
        Integer discountRate,
        Long maxDiscountAmount

) {
    public enum DiscountType {
        FIXED, RATE
    }
}
