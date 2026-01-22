package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    SYSTEM_ERROR(500, "SYSTEM_ERROR_001", "시스템 에러");
    private final int status;
    private final String code;
    private final String message;
}
