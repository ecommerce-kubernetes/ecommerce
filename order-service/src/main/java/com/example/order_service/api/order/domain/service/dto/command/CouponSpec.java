package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
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

    public static CouponSpec from(OrderCouponCalcResponse response) {
        if (response == null) {
            return null;
        }
        return of(response.getCouponId(), response.getCouponName(), response.getDiscountAmount());
    }

    public static CouponSpec of(Long couponId, String couponName, Long discountAmount){
        return CouponSpec.builder()
                .couponId(couponId)
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }
}
