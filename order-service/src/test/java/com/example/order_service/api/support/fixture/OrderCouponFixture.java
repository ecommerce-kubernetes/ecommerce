package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;

public class OrderCouponFixture {

    public static OrderCouponDiscountResponse.OrderCouponDiscountResponseBuilder anOrderCouponDiscountResponse() {
        return OrderCouponDiscountResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L);
    }

    public static OrderCouponInfo.OrderCouponInfoBuilder anOrderCouponInfo() {
        return OrderCouponInfo.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L);
    }
}
