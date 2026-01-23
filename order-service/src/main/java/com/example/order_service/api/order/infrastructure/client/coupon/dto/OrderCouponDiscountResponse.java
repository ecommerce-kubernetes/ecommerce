package com.example.order_service.api.order.infrastructure.client.coupon.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class OrderCouponDiscountResponse {
    private Long couponId;
    private String couponName;
    private Long discountAmount;
}
