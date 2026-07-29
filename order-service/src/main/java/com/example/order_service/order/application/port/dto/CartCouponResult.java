package com.example.order_service.order.application.port.dto;

import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import lombok.Builder;

@Builder
public record CartCouponResult(
        CartCouponSnapshot cartCoupon
) {
}
