package com.example.order_service.api.order.infrastructure.client.coupon.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCouponCalcResponse {
    private Long couponId;
    private Long discountAmount;
    private Long finalPaymentAmount;

    @Builder
    private OrderCouponCalcResponse(Long couponId, Long discountAmount, Long finalPaymentAmount) {
        this.couponId = couponId;
        this.discountAmount = discountAmount;
        this.finalPaymentAmount = finalPaymentAmount;
    }
}
