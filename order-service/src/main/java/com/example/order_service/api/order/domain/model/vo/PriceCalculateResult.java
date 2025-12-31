package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
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

    private static PriceCalculateResult of(OrderPriceInfo orderPriceInfo, AppliedCoupon appliedCoupon){
        return PriceCalculateResult.builder()
                .orderPriceInfo(orderPriceInfo)
                .appliedCoupon(appliedCoupon)
                .build();
    }
    public static PriceCalculateResult of(ItemCalculationResult itemCalculationResult, OrderCouponCalcResponse coupon, long useToPoint,
                                          long expectedPrice) {
        long couponDiscount = coupon != null ? coupon.getDiscountAmount() : 0L;
        long priceAfterCoupon = itemCalculationResult.getSubTotalPrice() - couponDiscount;
        long finalPaymentPrice = priceAfterCoupon - useToPoint;

        if(finalPaymentPrice != expectedPrice) {
            throw new OrderVerificationException("주문 금액이 변동되었습니다");
        }

        return PriceCalculateResult.of(OrderPriceInfo.from(itemCalculationResult, couponDiscount, useToPoint, finalPaymentPrice), AppliedCoupon.from(coupon));
    }
}
