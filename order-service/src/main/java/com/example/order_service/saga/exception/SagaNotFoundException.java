package com.example.order_service.saga.exception;

public class SagaNotFoundException extends SagaSystemException {
    public SagaNotFoundException(String message) {
        super(message);
    }
}
