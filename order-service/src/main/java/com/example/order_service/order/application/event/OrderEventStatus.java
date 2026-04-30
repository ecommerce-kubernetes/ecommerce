package com.example.order_service.order.application.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderEventStatus {
    SUCCESS("성공"),
    FAILURE("실패");
    private final String name;
}
