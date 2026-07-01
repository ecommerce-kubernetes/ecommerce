package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.application.GatewayException;

public class PaymentGatewayException extends GatewayException {
    private final PaymentErrorCode errorCode;

    public PaymentGatewayException(PaymentErrorCode errorCode, String externalErrorCode, String message) {
        super(externalErrorCode, message);
        this.errorCode = errorCode;
    }

    @Override
    public PaymentErrorCode errorCode() {
        return this.errorCode;
    }
}
