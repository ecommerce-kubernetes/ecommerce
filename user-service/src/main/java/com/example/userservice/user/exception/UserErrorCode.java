package com.example.userservice.user.exception;

import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "USER_001", "해당 유저를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(409, "USER_002", "이미 가입된 이메일입니다"),
    PASSWORD_NOT_MATCH(409, "PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다"),
    INSUFFICIENT_POINT(409, "USER_003", "포인트가 부족합니다"),
    SHIPPING_ADDRESS_NOT_FOUND(404, "USER_004", "배송지를 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
