package com.example.order_service.api.order.domain.service.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponSpec {
    private Long couponId;
    private String couponName;
    private Long discountAmount;

    @Builder
    private CouponSpec(Long couponId, String couponName, Long discountAmount){
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
    }
}
