package com.example.order_service.order.domain.saga;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaStep {
    INVENTORY_DEDUCT_PENDING("재고 감소", false),
    INVENTORY_RESTORE_PENDING("재고 복구", true),
    COUPON_USE_PENDING("쿠폰 무효화", false),
    COUPON_RESTORE_PENDING("쿠폰 복구", true),
    POINTS_DEDUCT_PENDING("포인트 감소", false),
    POINTS_RESTORE_PENDING("포인트 복구", true);
    private final String description;
    private final boolean compensation;
}
