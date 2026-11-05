package com.example.userservice.auth.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthUserPortErrorCode implements ErrorCode {
    INVALID_CREDENTIALS(ErrorCategory.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호를 확인해주세요."),
    USER_SYSTEM_ERROR(ErrorCategory.SYSTEM_ERROR, "SYSTEM_ERROR", "유저 포트 에러");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
