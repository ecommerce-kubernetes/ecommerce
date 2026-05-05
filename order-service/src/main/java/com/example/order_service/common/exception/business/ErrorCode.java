package com.example.order_service.common.exception.business;

public interface ErrorCode {
    String name();
    int getStatus();
    String getMessage();
    String getCode();
}
