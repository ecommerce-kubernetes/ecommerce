package com.example.order_service.payment.application.port.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentOrderStatus {
    PENDING("대기중"),
    COMPLETED("주문 완료"),
    PAID("결제 완료"),
    FAILED("주문 실패"),
    PAYMENT_FAILED("결제 실패"),
    CANCELED("취소됨");

    private final String name;
}
