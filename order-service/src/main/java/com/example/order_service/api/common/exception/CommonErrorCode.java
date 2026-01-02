package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{

    SERVICE_UNAVAILABLE(503, "SERVER_001", "현재 서비스 이용이 원활하지 않습니다 다시 시도해주세"),
    EXTERNAL_SYSTEM_ERROR(500, "SERVER_002", "외부 시스템 연동 중 오류가 발생했습니다");

    private final int status;
    private final String code;
    private final String message;
}
