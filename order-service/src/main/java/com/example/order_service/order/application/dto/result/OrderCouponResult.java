package com.example.order_service.order.application.dto.result;

import com.example.order_service.order.domain.model.vo.CouponValidationStatus;
import lombok.Builder;

public class OrderCouponResult {

    @Builder
    public record CouponValidation(
            CouponValidationStatus status,
            CouponBenefit couponBenefit
    ) {
    }

    @Builder
    public record CouponBenefit(
            Long couponId,
            String couponName,
            Long discountAmount
    ) {
    }
}
