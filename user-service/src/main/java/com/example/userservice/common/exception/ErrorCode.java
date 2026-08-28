package com.example.userservice.common.exception;

public interface ErrorCode {
    String name();
    int getStatus();
    String getMessage();
    String getCode();
}
