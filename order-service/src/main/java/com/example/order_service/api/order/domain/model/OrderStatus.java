package com.example.order_service.api.order.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기중"),
    PAYMENT_WAITING("결제 대기중"),
    COMPLETED("주문 완료"),
    PAYMENT_FAILED("결제 실패"),
    CANCELED("취소됨");

    private final String name;
}
