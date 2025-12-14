package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.model.Coupon;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AppliedCoupon {
    private Long couponId;
    private String couponName;
    private Long discountAmount;

    @Builder
    private AppliedCoupon(Long couponId, String couponName, Long discountAmount){
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
    }

    public static AppliedCoupon of(Long couponId, String couponName, Long discountAmount) {
        return AppliedCoupon.builder()
                .couponId(couponId)
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }

    public static AppliedCoupon from(OrderCouponCalcResponse coupon) {
        if (coupon == null) {
            return null;
        }
        return of(coupon.getCouponId(), coupon.getCouponName(), coupon.getDiscountAmount());
    }

    public static AppliedCoupon from(Coupon coupon){
        if(coupon == null) {
            return null;
        }
        return of(coupon.getCouponId(), coupon.getCouponName(), coupon.getDiscountAmount());
    }
}
