package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PriceCalculateResult {
    private long originTotalPrice;
    private long finalPaymentAmount;
    private OrderCouponCalcResponse coupon;
    private long useToPoint;

    @Builder
    private PriceCalculateResult(long originTotalPrice, long finalPaymentAmount, OrderCouponCalcResponse coupon, long useToPoint){
        this.originTotalPrice = originTotalPrice;
        this.finalPaymentAmount = finalPaymentAmount;
        this.coupon = coupon;
        this.useToPoint = useToPoint;
    }

    public static PriceCalculateResult of(long originTotalPrice, long finalPaymentAmount, OrderCouponCalcResponse coupon, long useToPoint){
        return PriceCalculateResult.builder()
                .originTotalPrice(originTotalPrice)
                .finalPaymentAmount(finalPaymentAmount)
                .coupon(coupon)
                .useToPoint(useToPoint)
                .build();
    }
}
