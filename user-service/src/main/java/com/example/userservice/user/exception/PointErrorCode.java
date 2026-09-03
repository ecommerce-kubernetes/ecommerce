package com.example.userservice.user.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {
    INSUFFICIENT_POINTS(ErrorCategory.BUSINESS_CONFLICT, "INSUFFICIENT_POINTS", "포인트가 부족합니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
