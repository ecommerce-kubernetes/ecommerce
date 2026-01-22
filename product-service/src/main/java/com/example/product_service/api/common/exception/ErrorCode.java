package com.example.product_service.api.common.exception;

public interface ErrorCode {
    String name();
    int getStatus();
    String getMessage();
    String getCode();
}
