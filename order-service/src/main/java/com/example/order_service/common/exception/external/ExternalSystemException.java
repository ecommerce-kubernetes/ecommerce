package com.example.order_service.common.exception.external;

import lombok.Getter;

@Getter
public class ExternalSystemException extends RuntimeException {
    private final String errorCode;

    public ExternalSystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ExternalSystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
