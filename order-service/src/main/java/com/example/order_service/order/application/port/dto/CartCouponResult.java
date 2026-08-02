package com.example.order_service.order.application.port.dto;

import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CartCouponResult(
        OrderCouponStatus status,
        CartCouponSnapshot cartCoupon,
        LocalDateTime expiresAt
) {
}
