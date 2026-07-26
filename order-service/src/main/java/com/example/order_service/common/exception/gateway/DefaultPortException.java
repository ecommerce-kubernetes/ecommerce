package com.example.order_service.common.exception.gateway;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DefaultPortException extends PortException {
    private final ErrorCode errorCode;
    public DefaultPortException(ErrorCode errorCode, String externalErrorCode, String message) {
        super(externalErrorCode, message);
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode errorCode() {
        return this.errorCode;
    }
}
