package com.example.order_service.common.exception.port;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserPortErrorCode implements ErrorCode {

    USER_CLIENT_ERROR(HttpStatus.CONFLICT, "USER_CLIENT_ERROR", "유저 조회중 오류가 발생했습니다."),
    USER_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "USER_SERVER_ERROR", "유저 시스템에 일시적인 오류가 발생했습니다."),
    USER_UNAVAILABLE_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "USER_UNAVAILABLE_SERVER_ERROR", "유저 시스템이 현재 응답할 수 없습니다. 잠시 후 다시 시도해주세요."),
    USER_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "USER_CIRCUIT_OPEN", "유저 시스템 연동이 일시적으로 지연되고 있습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
