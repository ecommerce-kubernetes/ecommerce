package com.example.order_service.saga.exception;

public class ExecutionNotFoundException extends SagaSystemException {
    public ExecutionNotFoundException(String message) {
        super(message);
    }
}
