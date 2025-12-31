package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderPriceInfo {
    private long totalOriginPrice;
    private long totalProductDiscount;
    private long couponDiscount;
    private long usedPoint;
    private long finalPaymentAmount;

    @Builder
    private OrderPriceInfo(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint,
                           long finalPaymentAmount){
        this.totalOriginPrice = totalOriginPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.couponDiscount = couponDiscount;
        this.usedPoint = usedPoint;
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public static OrderPriceInfo of(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint, long finalPaymentAmount){
        return OrderPriceInfo.builder()
                .totalOriginPrice(totalOriginPrice)
                .totalProductDiscount(totalProductDiscount)
                .couponDiscount(couponDiscount)
                .usedPoint(usedPoint)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }

    public static OrderPriceInfo from(Order order){
        return of(order.getTotalOriginPrice(), order.getTotalProductDiscount(), order.getCouponDiscount(), order.getPointDiscount(), order.getFinalPaymentAmount());
    }

    public static OrderPriceInfo from(ItemCalculationResult result, long couponDiscount, long usedPoint, long finalPaymentAmount) {
        return of(result.getTotalOriginalPrice(), result.getTotalProductDiscount(), couponDiscount, usedPoint, finalPaymentAmount);
    }
}
