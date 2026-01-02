package com.example.order_service.api.order.infrastructure.client.coupon.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCouponDiscountResponse {
    private Long couponId;
    private String couponName;
    private Long discountAmount;

    @Builder
    private OrderCouponDiscountResponse(Long couponId, String couponName, Long discountAmount) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
    }
}
