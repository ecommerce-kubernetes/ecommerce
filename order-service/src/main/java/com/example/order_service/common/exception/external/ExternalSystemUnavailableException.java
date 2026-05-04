package com.example.order_service.common.exception.external;

public class ExternalSystemUnavailableException extends ExternalSystemException {
    public ExternalSystemUnavailableException(String message) {
        super(message);
    }

    public ExternalSystemUnavailableException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ExternalSystemUnavailableException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
