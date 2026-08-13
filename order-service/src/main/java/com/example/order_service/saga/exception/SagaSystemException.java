package com.example.order_service.saga.exception;

public class SagaSystemException extends RuntimeException {
    public SagaSystemException(String message) {
        super(message);
    }
}
