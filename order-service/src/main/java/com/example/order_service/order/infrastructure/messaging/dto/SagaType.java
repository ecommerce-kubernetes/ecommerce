package com.example.order_service.order.infrastructure.messaging.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaType {
    DEDUCT_INVENTORY("재고 감소"),
    RESTORE_INVENTORY("재고 복구"),
    USED_COUPON("쿠폰 사용"),
    RESTORE_COUPON("쿠폰 복구"),
    DEDUCT_POINTS("포인트 감소"),
    RESTORE_POINTS("포인트 복구");

    private final String description;
}
