package com.example.order_service.api.common.exception.external;

public class ExternalSystemException extends RuntimeException {
    public ExternalSystemException(String message) {
        super(message);
    }

    public ExternalSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
