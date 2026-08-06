package com.example.order_service.common.exception;

import lombok.Getter;

@Getter
public class DefaultPortException extends PortException {
    private final ErrorCode errorCode;
    public DefaultPortException(ErrorCode errorCode, String externalErrorCode, String message) {
        super(errorCode, externalErrorCode, message);
        this.errorCode = errorCode;
    }
}
