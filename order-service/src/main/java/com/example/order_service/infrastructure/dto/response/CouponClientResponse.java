package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

import java.util.List;

public class CouponClientResponse {

    @Builder
    public record Calculate(
            CartCoupon cartCoupon,
            List<ItemCoupon> itemCoupons
    ) {
    }

    @Builder
    public record CartCoupon(
            Long couponId,
            String couponName,
            Long discountAmount
    ) {
    }

    @Builder
    public record ItemCoupon(
            Long productVariantId,
            Long couponId,
            String couponName,
            Long discountAmount
    ) {
    }
}
