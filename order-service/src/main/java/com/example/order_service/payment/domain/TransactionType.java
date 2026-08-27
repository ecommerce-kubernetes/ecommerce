package com.example.order_service.payment.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionType {
    PAYMENT("결제"),
    REFUND("환불");
    private final String description;
}
