package com.example.order_service.order.domain.saga;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaStatus {
    STARTED("진행중"),
    COMPENSATING("보상 트랜잭션 진행중"),;
    private final String description;
}
