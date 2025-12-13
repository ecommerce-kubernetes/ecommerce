package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PriceCalculateResult {
    private PaymentInfo paymentInfo;
    private AppliedCoupon appliedCoupon;

    @Builder
    private PriceCalculateResult(PaymentInfo paymentInfo, AppliedCoupon appliedCoupon){
        this.paymentInfo = paymentInfo;
        this.appliedCoupon = appliedCoupon;
    }

    public static PriceCalculateResult of(PaymentInfo paymentInfo, AppliedCoupon appliedCoupon){
        return PriceCalculateResult.builder()
                .paymentInfo(paymentInfo)
                .appliedCoupon(appliedCoupon)
                .build();
    }

    public static PriceCalculateResult from(ItemCalculationResult itemResult, OrderCouponCalcResponse coupon, long usedPoint,
                                            long finalPaymentAmount){
        long couponDiscount = (coupon != null) ? coupon.getDiscountAmount() : 0;
        return of(PaymentInfo.from(itemResult, couponDiscount, usedPoint, finalPaymentAmount), AppliedCoupon.from(coupon));
    }
}
