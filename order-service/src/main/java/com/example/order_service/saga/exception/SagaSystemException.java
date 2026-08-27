package com.example.order_service.saga.exception;

import lombok.Getter;

@Getter
public class SagaSystemException extends RuntimeException {
    private final SagaErrorCode errorCode;
    public SagaSystemException(SagaErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
