package com.example.order_service.exception;

public class OrderVerificationException extends RuntimeException {
    public OrderVerificationException(String message) {
        super(message);
    }
}
