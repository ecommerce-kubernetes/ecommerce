package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.order.domain.vo.CartCouponSnapshot;
import lombok.Builder;

@Builder
public record CartCouponResult(
        CartCouponSnapshot cartCoupon
) {
}
