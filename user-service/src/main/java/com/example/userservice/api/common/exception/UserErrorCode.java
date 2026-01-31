package com.example.userservice.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "USER_001", "해당 유저를 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
