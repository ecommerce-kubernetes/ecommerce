package com.example.userservice.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    PASSWORD_NOT_MATCH(409, "AUTH_001", "비밀번호가 일치하지 않습니다");
    private final int status;
    private final String code;
    private final String message;
}
