package com.example.order_service.saga.exception;

public class ProcessorNotFoundException extends SagaSystemException {
    public ProcessorNotFoundException(String message) {
        super(message);
    }
}
