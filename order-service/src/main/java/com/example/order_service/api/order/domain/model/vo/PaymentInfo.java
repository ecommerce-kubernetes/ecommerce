package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentInfo {
    private long totalOriginPrice;
    private long totalProductDiscount;
    private long couponDiscount;
    private long usedPoint;
    private long finalPaymentAmount;

    @Builder
    private PaymentInfo(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint,
                        long finalPaymentAmount){
        this.totalOriginPrice = totalOriginPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.couponDiscount = couponDiscount;
        this.usedPoint = usedPoint;
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public static PaymentInfo of(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint, long finalPaymentAmount){
        return PaymentInfo.builder()
                .totalOriginPrice(totalOriginPrice)
                .totalProductDiscount(totalProductDiscount)
                .couponDiscount(couponDiscount)
                .usedPoint(usedPoint)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }

    public static PaymentInfo from(ItemCalculationResult result, long couponDiscount, long usedPoint, long finalPaymentAmount) {
        return of(result.getTotalOriginalPrice(), result.getTotalProductDiscount(), couponDiscount, usedPoint, finalPaymentAmount);
    }
}
