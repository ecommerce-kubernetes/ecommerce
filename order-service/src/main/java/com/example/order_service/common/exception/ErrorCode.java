package com.example.order_service.common.exception;

public interface ErrorCode {
    String name();
    ErrorCategory getCategory();
    String getMessage();
    String getCode();
}
