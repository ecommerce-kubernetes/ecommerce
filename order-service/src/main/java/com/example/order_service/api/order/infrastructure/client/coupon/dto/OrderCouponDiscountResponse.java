package com.example.order_service.api.order.infrastructure.client.coupon.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCouponDiscountResponse {
    private Long couponId;
    private String couponName;
    private Long discountAmount;
}
