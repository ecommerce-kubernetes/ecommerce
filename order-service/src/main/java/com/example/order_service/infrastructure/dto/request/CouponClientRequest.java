package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

public class CouponClientRequest {

    @Builder
    public record Calculate (
            Long userId,
            Long couponId,
            Long totalAmount
    ) {
        public static Calculate of(Long userId, Long couponId, Long totalAmount) {
            return Calculate.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .totalAmount(totalAmount)
                    .build();
        }
    }
}
