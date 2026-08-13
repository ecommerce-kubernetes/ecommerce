package com.example.order_service.saga.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaStep {
    INVENTORY_DEDUCT_PENDING("재고 감소"),
    INVENTORY_RESTORE_PENDING("재고 복구"),
    COUPON_USE_PENDING("쿠폰 무효화"),
    COUPON_RESTORE_PENDING("쿠폰 복구"),
    POINTS_DEDUCT_PENDING("포인트 감소"),
    POINTS_RESTORE_PENDING("포인트 복구"),
    END("완료"),

    INVENTORY("재고"),
    COUPON("쿠폰"),
    POINT("포인트");

    private final String description;
}
