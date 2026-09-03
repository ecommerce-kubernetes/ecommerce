package com.example.userservice.outbox.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxErrorCode implements ErrorCode {

    OUTBOX_NOT_FOUND(ErrorCategory.NOT_FOUND, "OUTBOX_NOT_FOUND", "아웃박스를 찾을 수 없습니다"),
    INVALID_OUTBOX_MESSAGE_STATUS(ErrorCategory.BUSINESS_CONFLICT, "INVALID_OUTBOX_MESSAGE_STATUS", "PENDING 상태의 아웃박스 메시지만 SENT로 변경할 수 있습니다");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
