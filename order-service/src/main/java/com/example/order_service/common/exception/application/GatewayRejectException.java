package com.example.order_service.common.exception.application;

public class GatewayRejectException extends GatewayException {
    public GatewayRejectException(ErrorCode errorCode, String code, String message) {
        super(errorCode, code, message);
    }
}
