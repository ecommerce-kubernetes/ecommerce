package com.example.order_service.exception;

public class InsufficientException extends RuntimeException {
    public InsufficientException(String message) {
        super(message);
    }
}
