package com.example.userservice.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    PASSWORD_NOT_MATCH(409, "AUTH_001", "비밀번호가 일치하지 않습니다"),
    REFRESH_TOKEN_MISSING(401, "AUTH_002", "리프레시 토큰이 존재하지 않습니다"),
    REFRESH_TOKEN_EXPIRED(401, "AUTH_003", "리프레시 토큰이 만료되었습니다"),
    REFRESH_TOKEN_INVALID(401, "AUTH_004", "유효하지 않은 토큰입니다");
    private final int status;
    private final String code;
    private final String message;
}
