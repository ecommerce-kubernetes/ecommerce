package com.example.order_service.payment.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionType {
    PAYMENT("결제"),
    REFOUND("환불");
    private final String description;
}
