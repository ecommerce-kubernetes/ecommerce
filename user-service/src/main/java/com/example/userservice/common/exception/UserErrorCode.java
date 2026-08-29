package com.example.userservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "USER_001", "해당 유저를 찾을 수 없습니다"),
    PASSWORD_NOT_MATCH(409, "PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다"),
    INSUFFICIENT_POINT(409, "USER_003", "포인트가 부족합니다");
    private final int status;
    private final String code;
    private final String message;
}
