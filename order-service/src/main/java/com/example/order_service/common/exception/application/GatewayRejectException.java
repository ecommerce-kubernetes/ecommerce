package com.example.order_service.common.exception.application;

public class GatewayRejectException extends GatewayException {
    public GatewayRejectException(ErrorCode errorCode) {
        super(errorCode);
    }
}
