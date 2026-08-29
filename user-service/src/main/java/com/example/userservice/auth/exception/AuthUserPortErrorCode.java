package com.example.userservice.auth.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthUserPortErrorCode implements ErrorCode {

    USER_SERVER_ERROR(ErrorCategory.SYSTEM_ERROR, "USER_SERVER_ERROR", "유저 포트 에러");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
