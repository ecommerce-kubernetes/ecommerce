package com.example.userservice.auth.exception;

import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    REFRESH_TOKEN_NOT_FOUND(404, "TOKEN_NOT_FOUND", "토큰이 존재하지 않습니다"),
    PASSWORD_NOT_MATCH(409, "AUTH_001", "비밀번호가 일치하지 않습니다"),
    REFRESH_TOKEN_EXPIRED(401, "AUTH_003", "리프레시 토큰이 만료되었습니다"),
    REFRESH_TOKEN_NOT_MATCHES(403, "REFRESH_TOKEN_NOT_MATCHES", "리프레시 토큰이 동일하지 않습니다"),
    REFRESH_TOKEN_INVALID(401, "AUTH_004", "유효하지 않은 토큰입니다");
    private final int status;
    private final String code;
    private final String message;
}
