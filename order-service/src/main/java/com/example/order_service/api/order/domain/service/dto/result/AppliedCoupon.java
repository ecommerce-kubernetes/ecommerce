package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AppliedCoupon {
    private Long couponId;
    private String couponName;

    @Builder
    private AppliedCoupon(Long couponId, String couponName){
        this.couponId = couponId;
        this.couponName = couponName;
    }
}
