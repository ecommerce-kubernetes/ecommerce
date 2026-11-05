package com.example.order_service.saga.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaStep {
    INVENTORY("재고"),
    COUPON("쿠폰"),
    POINT("포인트"),
    END("완료");

    private final String description;
}
