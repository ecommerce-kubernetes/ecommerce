package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

import java.util.List;

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
            String scope,
            Boolean available,
            Long discountAmount,
            String code,
            List<Long> applicableVariantIds
    ) {}

}
