package com.example.order_service.api.order.saga.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SagaStep {
    PRODUCT("상품 서비스"),
    COUPON("쿠폰 서비스"),
    USER("유저 서비스");

    private final String name;
}
