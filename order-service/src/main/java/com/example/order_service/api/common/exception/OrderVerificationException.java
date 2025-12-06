package com.example.order_service.api.common.exception;

public class OrderVerificationException extends RuntimeException {
    public OrderVerificationException(String message) {
        super(message);
    }
}
