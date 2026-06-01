package com.example.order_service.payment.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    DONE("완료"),
    CANCELED("결제 취소"),
    WAITING_FOR_DEPOSIT("입금 대기");
    private final String description;
}
