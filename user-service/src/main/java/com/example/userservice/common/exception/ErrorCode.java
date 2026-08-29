package com.example.userservice.common.exception;

public interface ErrorCode {
    String name();
    ErrorCategory getCategory();
    String getMessage();
    String getCode();
}
