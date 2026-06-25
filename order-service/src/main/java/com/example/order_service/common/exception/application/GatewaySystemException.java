package com.example.order_service.common.exception.application;

public class GatewaySystemException extends GatewayException {
    public GatewaySystemException(ErrorCode errorCode, String code, String message) {
        super(errorCode, code, message);
    }
}
