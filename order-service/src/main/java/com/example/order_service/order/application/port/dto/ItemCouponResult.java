package com.example.order_service.order.application.port.dto;

import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import lombok.Builder;

@Builder
public record ItemCouponResult(
        ItemCouponSnapshot itemCoupon
) {
}
