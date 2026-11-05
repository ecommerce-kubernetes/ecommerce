package com.example.order_service.common.exception.external;

public class ExternalSystemUnavailableException extends ExternalSystemException {
    public ExternalSystemUnavailableException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ExternalSystemUnavailableException(String errorCode, String message) {
        super(errorCode, message);
    }
}
