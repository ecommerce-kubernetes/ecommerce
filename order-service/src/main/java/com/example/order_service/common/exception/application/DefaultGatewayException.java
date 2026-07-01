package com.example.order_service.common.exception.application;

import lombok.Getter;

@Getter
public class DefaultGatewayException extends GatewayException {
    private final ErrorCode errorCode;
    public DefaultGatewayException(ErrorCode errorCode, String externalErrorCode, String message) {
        super(externalErrorCode, message);
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode errorCode() {
        return this.errorCode;
    }
}
