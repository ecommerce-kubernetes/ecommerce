package com.example.order_service.saga.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaErrorCode {
    SAGA_MESSAGE_SERIALIZATION_FAILED("SAGA_MESSAGE_SERIALIZATION_FAILED", "사가 메시지 직렬화에 실패했습니다."),
    NOT_FOUND_SAGA("NOT_FOUND_SAGA", "사가를 찾을 수 없습니다"),
    NOT_FOUND_EXECUTION("NOT_FOUND_EXECUTION", "사가 작업을 찾을 수 없습니다"),
    ALREADY_FAILED_EXECUTION("ALREADY_FAILED_EXECUTION", "이미 실패한 사가 작업입니다"),
    ALREADY_SUCCEED_EXECUTION("ALREADY_SUCCEED_EXECUTION", "이미 성공한 사가 작업입니다."),
    UNSUPPORTED_SAGA_EVENT("UNSUPPORTED_SAGA_EVENT", "지원하지 않는 사가 이벤트 입니다.");
    private final String code;
    private final String message;
}
