package com.example.order_service.api.common.exception.external;

public class ExternalSystemUnavailableException extends ExternalSystemException {
    public ExternalSystemUnavailableException(String message) {
        super(message);
    }

    public ExternalSystemUnavailableException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
