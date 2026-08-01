package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record ItemCouponsRequest(
        List<Long> itemCouponIds
) {
}
