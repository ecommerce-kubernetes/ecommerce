package com.example.order_service.common.exception.external;

public class ExternalClientException extends ExternalSystemException {
    public ExternalClientException(String message) {
        super(message);
    }
    public ExternalClientException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ExternalClientException(String errorCode, String message) {
        super(errorCode, message);
    }
}
