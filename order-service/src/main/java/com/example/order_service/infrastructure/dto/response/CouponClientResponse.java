package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

public class CouponClientResponse {

    //TODO Deprecated
    @Builder
    public record Calculate(
            String code,
            DiscountBenefit discountBenefit
    ) {
    }

    @Builder
    public record DiscountBenefit(
            Long couponId,
            String couponName,
            Long discountAmount
    ) {
    }

    @Builder
    public record CouponInfo(
            Long couponId,
            String couponName,
            Boolean available,
            Long discountAmount,
            String code
    ) {}

}
