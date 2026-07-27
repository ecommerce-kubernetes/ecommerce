package com.example.order_service.common.exception.port;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public abstract class PortException extends RuntimeException {
    private final String externalErrorCode;

    public PortException(String externalErrorCode, String message) {
        super(String.format("Gateway Error: [%s] %s", externalErrorCode, message));
        this.externalErrorCode = externalErrorCode;
    }

    public abstract ErrorCode errorCode();
}
