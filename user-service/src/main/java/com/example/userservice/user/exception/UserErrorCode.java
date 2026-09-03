package com.example.userservice.user.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(ErrorCategory.NOT_FOUND, "USER_NOT_FOUND", "해당 유저를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(ErrorCategory.BUSINESS_CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다"),
    PASSWORD_NOT_MATCH(ErrorCategory.BUSINESS_CONFLICT, "PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
