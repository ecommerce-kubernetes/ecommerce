package com.example.order_service.api.order.domain.service.dto.result;

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
}
