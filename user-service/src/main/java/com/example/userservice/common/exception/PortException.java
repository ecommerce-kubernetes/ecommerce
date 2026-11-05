package com.example.userservice.common.exception;

import lombok.Getter;

@Getter
public class PortException extends RuntimeException {
    private final ErrorCode errorCode;

    public PortException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
