package com.example.order_service.api.order.facade.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderEventStatus {
    SUCCESS("성공"),
    FAILURE("실패");
    private final String name;
}
