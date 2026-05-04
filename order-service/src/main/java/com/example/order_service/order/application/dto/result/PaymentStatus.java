package com.example.order_service.order.application.dto.result;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    DONE("완료"),
    CANCELED("결제 취소"),
    UNKNOWN("알 수 없음");
    private final String description;
}
