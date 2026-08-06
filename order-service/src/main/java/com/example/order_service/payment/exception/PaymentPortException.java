package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.PortException;

public class PaymentPortException extends PortException {
    private final PaymentErrorCode errorCode;

    public PaymentPortException(PaymentErrorCode errorCode, String externalErrorCode, String message) {
        super(errorCode, externalErrorCode, message);
        this.errorCode = errorCode;
    }

    public PaymentErrorCode errorCode() {
        return this.errorCode;
    }
}
