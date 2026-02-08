package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderCouponInfo {
    private Long couponId;
    private String couponName;
    private Long discountAmount;

    public static OrderCouponInfo notUsed() {
        return OrderCouponInfo.builder()
                .couponId(null)
                .couponName(null)
                .discountAmount(0L)
                .build();
    }
}
