package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.gateway.PortException;

public class PaymentPortException extends PortException {
    private final PaymentErrorCode errorCode;

    public PaymentPortException(PaymentErrorCode errorCode, String externalErrorCode, String message) {
        super(externalErrorCode, message);
        this.errorCode = errorCode;
    }

    @Override
    public PaymentErrorCode errorCode() {
        return this.errorCode;
    }
}
