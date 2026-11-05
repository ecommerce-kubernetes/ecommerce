package com.example.order_service.infrastructure.dto.response.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ItemCouponsResponse(
        Long userId,
        List<ItemCoupon> itemCoupons

) {

    @Builder
    public record ItemCoupon(
            Long itemCouponId,
            String status,
            String name,
            Integer applyQuantityLimit,
            String discountType,
            Long discountAmount,
            Integer discountRate,
            Long maxDiscountAmount,
            @JsonFormat(
                    shape = JsonFormat.Shape.STRING,
                    pattern = "yyyy-MM-dd HH:mm:ss",
                    timezone = "Asia/Seoul"
            )
            LocalDateTime expiresAt
    ) {
    }
}
