package com.example.product_service.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT_VALUE(ErrorCategory.INVALID_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다"),
    SYSTEM_ERROR(ErrorCategory.SYSTEM_ERROR, "SYSTEM_ERROR_001", "시스템 에러");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
