package com.example.order_service.saga.domain.tmp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StepResult {
    COMPLETED("SAGA 성공"),
    FAILED("SAGA 실패");
    private final String description;
}
