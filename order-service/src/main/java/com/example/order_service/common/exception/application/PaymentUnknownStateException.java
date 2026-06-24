package com.example.order_service.common.exception.application;

public class PaymentUnknownStateException extends GatewayException {
    public PaymentUnknownStateException(ErrorCode errorCode) {
        super(errorCode);
    }
}
