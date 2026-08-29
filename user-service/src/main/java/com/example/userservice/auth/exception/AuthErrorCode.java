package com.example.userservice.auth.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    REFRESH_TOKEN_NOT_FOUND(ErrorCategory.NOT_FOUND, "REFRESH_TOKEN_NOT_FOUND", "토큰이 존재하지 않습니다"),
    REFRESH_TOKEN_EXPIRED(ErrorCategory.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다"),
    REFRESH_TOKEN_NOT_MATCHES(ErrorCategory.FORBIDDEN, "REFRESH_TOKEN_NOT_MATCHES", "리프레시 토큰이 동일하지 않습니다"),
    REFRESH_TOKEN_INVALID(ErrorCategory.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "유효하지 않은 토큰입니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
