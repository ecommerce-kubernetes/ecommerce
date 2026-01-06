package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PriceCalculateResult {
    private OrderPriceInfo orderPriceInfo;
    private AppliedCoupon appliedCoupon;

    @Builder(access = AccessLevel.PRIVATE)
    private PriceCalculateResult(OrderPriceInfo orderPriceInfo, AppliedCoupon appliedCoupon){
        this.orderPriceInfo = orderPriceInfo;
        this.appliedCoupon = appliedCoupon;
    }

    public static PriceCalculateResult of(ItemCalculationResult itemCalculationResult, OrderCouponDiscountResponse coupon,
                                          Long couponDiscount, Long useToPoint, Long finalPaymentAmount) {
        return PriceCalculateResult.builder()
                .orderPriceInfo(OrderPriceInfo.from(itemCalculationResult, couponDiscount, useToPoint, finalPaymentAmount))
                .appliedCoupon(AppliedCoupon.from(coupon))
                .build();
    }
}
