package com.example.order_service.exception.server;

public class UnavailableServerException extends RuntimeException {
    public UnavailableServerException(String message) {
        super(message);
    }
}
