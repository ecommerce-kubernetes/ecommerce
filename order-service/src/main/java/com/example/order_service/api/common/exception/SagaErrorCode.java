package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaErrorCode implements ErrorCode{
    SAGA_NOT_FOUND(404, "SAGA_001", "SAGA 인스턴스를 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
