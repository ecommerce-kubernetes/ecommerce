package com.example.order_service.api.order.infrastructure.client.coupon.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderCouponCalcRequest {
    private Long userId;
    private Long couponId;
    private Long totalAmount;

    @Builder
    private OrderCouponCalcRequest(Long userId, Long couponId, Long totalAmount){
        this.userId = userId;
        this.couponId = couponId;
        this.totalAmount = totalAmount;
    }

    public static OrderCouponCalcRequest of(Long userId, Long couponId, Long totalAmount){
        return OrderCouponCalcRequest.builder()
                .userId(userId)
                .couponId(couponId)
                .totalAmount(totalAmount)
                .build();
    }
}
