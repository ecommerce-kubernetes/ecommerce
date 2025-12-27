package com.example.order_service.api.common.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final PaymentErrorCode errorCode;
    public PaymentException(String message, PaymentErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
