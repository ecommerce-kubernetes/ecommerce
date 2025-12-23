package com.example.order_service.api.order.application.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderResultStatus {
    SUCCESS("주문 성공"),
    FAILURE("주문 실패");
    private final String name;
}
