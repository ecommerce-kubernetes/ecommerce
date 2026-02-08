package com.example.order_service.api.order.saga.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SagaStep {
    PRODUCT("상품 재고 단계"),
    COUPON("쿠폰 단계"),
    USER("유저 포인트 단계"),
    PAYMENT("결제 단계");

    private final String name;
}
