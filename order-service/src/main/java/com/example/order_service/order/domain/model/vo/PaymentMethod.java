package com.example.order_service.order.domain.model.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드"),
    EASY_PAYMENT("간편 결제"),
    UNKNOWN("알 수 없음");
    private final String description;
}
