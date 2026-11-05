package com.example.userservice.user.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserPortErrorCode implements ErrorCode {

    SAGA_MESSAGE_SERIALIZATION_FAILED(ErrorCategory.SYSTEM_ERROR, "SYSTEM_ERROR", "데이터 직렬화/역직렬화 중 시스템 오류가 발생했습니다");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
