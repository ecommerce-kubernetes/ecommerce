package com.example.order_service.api.common.exception;

public class InsufficientException extends RuntimeException {
    public InsufficientException(String message) {
        super(message);
    }
}
