package com.example.order_service.common.exception;

import lombok.Getter;

@Getter
public class PortException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String externalErrorCode;

    public PortException(ErrorCode errorCode, String externalErrorCode, String message) {
        super(String.format("Port Error: [%s] %s", externalErrorCode, message));
        this.errorCode = errorCode;
        this.externalErrorCode = externalErrorCode;
    }
}
