package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderPriceInfo {
    private long totalOriginPrice;
    private long totalProductDiscount;
    private long couponDiscount;
    private long pointDiscount;
    private long finalPaymentAmount;

    @Builder
    private OrderPriceInfo(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long pointDiscount,
                           long finalPaymentAmount){
        this.totalOriginPrice = totalOriginPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.couponDiscount = couponDiscount;
        this.pointDiscount = pointDiscount;
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public static OrderPriceInfo of(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint, long finalPaymentAmount){
        return OrderPriceInfo.builder()
                .totalOriginPrice(totalOriginPrice)
                .totalProductDiscount(totalProductDiscount)
                .couponDiscount(couponDiscount)
                .pointDiscount(usedPoint)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }

    public static OrderPriceInfo from(OrderProductAmount result, long couponDiscount, long usedPoint, long finalPaymentAmount) {
        return of(result.getTotalOriginalAmount(), result.getTotalDiscountAmount(), couponDiscount, usedPoint, finalPaymentAmount);
    }
}
