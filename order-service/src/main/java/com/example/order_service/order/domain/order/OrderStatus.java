package com.example.order_service.order.domain.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기중"),
    COMPLETED("주문 완료"),
    ACCEPTED("주문 접수"),
    FAILED("주문 실패"),
    CANCELED("취소됨");

    private final String name;
}
