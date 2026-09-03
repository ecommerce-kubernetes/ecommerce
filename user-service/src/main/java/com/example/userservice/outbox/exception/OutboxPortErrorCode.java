package com.example.userservice.outbox.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxPortErrorCode implements ErrorCode {

    MESSAGE_DESERIALIZATION_ERROR(ErrorCategory.SYSTEM_ERROR, "SYSTEM_ERROR", "메시지 역직렬화에 실패했습니다.");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
