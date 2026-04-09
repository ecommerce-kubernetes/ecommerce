package com.example.userservice.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값 입니다"),
    INVALID_DATE_FORMAT(400, "COMMON_002", "잘못된 날짜 형식 입니다"),
    INVALID_TYPE_VALUE(400, "COMMON_003", "요청 데이터 형식이 올바르지 않습니다");
    private final int status;
    private final String code;
    private final String message;
}
