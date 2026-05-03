package com.example.order_service.infrastructure.dto.response;

import lombok.Builder;

public class CouponClientResponse {

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
}
