package com.example.order_service.order.infrastructure.messaging.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaResult {
    SUCCESS("성공"),
    FAILURE("실패");

    private final String description;
}
