package com.example.order_service.order.application.port.dto.result;

import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import lombok.Builder;

@Builder
public record ItemCouponResult(
        ItemCouponSnapshot itemCoupon
) {
}
