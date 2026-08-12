package com.example.order_service.saga.domain.tmp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaStatus {
    STARTED("진행중"),
    COMPENSATING("보상 트랜잭션 진행중"),
    COMPLETE("사가 완료"),
    FAILED("사가 실패");
    private final String description;
}
