package com.example.userservice.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaErrorCode implements ErrorCode {
    POINT_REFOUND_FAILED(500, "SAGA_001", "포인트 복구 실패");
    private final int status;
    private final String code;
    private final String message;
}
