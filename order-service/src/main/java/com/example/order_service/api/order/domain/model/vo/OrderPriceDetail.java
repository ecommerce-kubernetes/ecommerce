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
public class OrderPriceDetail {
    private long totalOriginPrice;
    private long totalProductDiscount;
    private long couponDiscount;
    private long pointDiscount;
    private long finalPaymentAmount;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderPriceDetail(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long pointDiscount,
                             long finalPaymentAmount){
        this.totalOriginPrice = totalOriginPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.couponDiscount = couponDiscount;
        this.pointDiscount = pointDiscount;
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public static OrderPriceDetail of(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long usedPoint, long finalPaymentAmount){
        return OrderPriceDetail.builder()
                .totalOriginPrice(totalOriginPrice)
                .totalProductDiscount(totalProductDiscount)
                .couponDiscount(couponDiscount)
                .pointDiscount(usedPoint)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }
}
