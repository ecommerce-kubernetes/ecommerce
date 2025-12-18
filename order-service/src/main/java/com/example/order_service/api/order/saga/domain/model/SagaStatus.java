package com.example.order_service.api.order.saga.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SagaStatus {
    STARTED("전송"),
    FINISHED("완료"),
    COMPENSATING("보상중"),
    FAILED("실패");
    private final String name;
}
