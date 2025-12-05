package com.example.order_service.exception.server;

public class UnavailableServiceException extends RuntimeException {
    public UnavailableServiceException(String message) {
        super(message);
    }
}
