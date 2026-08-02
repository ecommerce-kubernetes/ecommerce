package com.example.order_service.infrastructure.dto.response.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CartCouponResponse(
        Long userId,
        Long cartCouponId,
        String status,
        String name,
        Long minimumPaymentAmount,
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
