package com.example.order_service.saga.exception;

public class InvalidSagaStateException extends SagaSystemException {
    public InvalidSagaStateException(String message) {
        super(message);
    }
}
