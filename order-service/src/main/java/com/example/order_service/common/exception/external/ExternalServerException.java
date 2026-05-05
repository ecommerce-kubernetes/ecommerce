package com.example.order_service.common.exception.external;

public class ExternalServerException extends ExternalSystemException {
    public ExternalServerException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    public ExternalServerException(String errorCode, String message) {
        super(errorCode, message);
    }
}
