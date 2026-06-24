package com.example.order_service.common.exception.application;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {
    private final ErrorCode errorCode;

    public GatewayException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
