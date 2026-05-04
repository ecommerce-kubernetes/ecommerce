package com.example.order_service.common.exception.external;

public class ExternalServerException extends ExternalSystemException {
    public ExternalServerException(String message) {
        super(message);
    }

    public ExternalServerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ExternalServerException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ExternalServerException(String errorCode, String message) {
        super(errorCode, message);
    }
}
