package com.example.order_service.common.exception.external;

public class ExternalNetworkTimeoutException extends ExternalSystemException {

    public ExternalNetworkTimeoutException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalNetworkTimeoutException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
