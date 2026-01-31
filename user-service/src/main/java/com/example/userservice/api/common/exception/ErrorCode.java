package com.example.userservice.api.common.exception;

public interface ErrorCode {
    String name();
    int getStatus();
    String getMessage();
    String getCode();
}
