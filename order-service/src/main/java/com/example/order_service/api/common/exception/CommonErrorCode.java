package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_ERROR(500, "SERVER_001", "서버 에러"),
    UNKNOWN_ERROR(500, "SERVER_002", "알 수 없는 에러");
    private final int status;
    private final String code;
    private final String message;
}
