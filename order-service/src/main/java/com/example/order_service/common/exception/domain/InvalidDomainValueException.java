package com.example.order_service.common.exception.domain;

public class InvalidDomainValueException extends RuntimeException {
    public InvalidDomainValueException(String message) {
        super(message);
    }
}
