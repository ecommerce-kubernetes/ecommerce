package com.example.order_service.common.exception.application;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String code;
    private final String message;

    public GatewayException(ErrorCode errorCode, String code, String message) {
        super(String.format("Gateway Error: [%s] %s", code, message));
        this.errorCode = errorCode;
        this.code = code;
        this.message = message;
    }
}
