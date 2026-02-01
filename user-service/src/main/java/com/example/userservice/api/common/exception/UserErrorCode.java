package com.example.userservice.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "USER_001", "해당 유저를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(409, "USER_002", "동일한 이메일의 유저가 존재합니다"),
    INSUFFICIENT_POINT(409, "USER_003", "포인트가 부족합니다");
    private final int status;
    private final String code;
    private final String message;
}
