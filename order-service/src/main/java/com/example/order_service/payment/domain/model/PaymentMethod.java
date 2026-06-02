package com.example.order_service.payment.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드"),
    EASY_PAYMENT("간편 결제");
    private final String description;
}
