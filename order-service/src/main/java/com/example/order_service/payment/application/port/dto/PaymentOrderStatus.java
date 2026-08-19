package com.example.order_service.payment.application.port.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentOrderStatus {
    PENDING("대기중"),
    ACCEPTED("주문 접수"),
    COMPLETED("주문 완료"),
    FAILED("주문 실패"),
    CANCELED("취소됨");

    private final String name;
}
