package com.example.userservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{
    INVALID_INPUT_VALUE(ErrorCategory.INVALID_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다"),
    SAGA_RETRY_EXHAUSTED(ErrorCategory.SYSTEM_ERROR, "SYSTEM_RETRY_EXHAUSTED", "재시도 횟수를 초과하여 처리에 실패했습니다");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
