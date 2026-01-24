package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PriceCalculateResult {
    private OrderPriceDetail orderPriceDetail;
    private CouponInfo couponInfo;

    @Builder(access = AccessLevel.PRIVATE)
    private PriceCalculateResult(OrderPriceDetail orderPriceDetail, CouponInfo couponInfo){
        this.orderPriceDetail = orderPriceDetail;
        this.couponInfo = couponInfo;
    }

    public static PriceCalculateResult of(OrderProductAmount orderProductAmount, OrderCouponDiscountResponse coupon,
                                          Long couponDiscount, Long useToPoint, Long finalPaymentAmount) {
        return null;
    }
}
