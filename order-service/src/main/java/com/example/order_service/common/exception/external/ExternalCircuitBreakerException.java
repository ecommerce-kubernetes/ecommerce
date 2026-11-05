package com.example.order_service.common.exception.external;

import lombok.Getter;

@Getter
public class ExternalCircuitBreakerException extends ExternalSystemException {

    public ExternalCircuitBreakerException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalCircuitBreakerException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
