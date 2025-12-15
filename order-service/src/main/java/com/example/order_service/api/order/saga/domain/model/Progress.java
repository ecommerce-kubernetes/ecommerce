package com.example.order_service.api.order.saga.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Progress {
    STARTED("전송"),
    COMPLETED("완료"),
    FAILED("실패"),
    COMPENSATING("보상중"),
    COMPENSATED("보상 완료");
    private final String name;
}
