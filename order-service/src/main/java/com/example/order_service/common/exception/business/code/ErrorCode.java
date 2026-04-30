package com.example.order_service.common.exception.business.code;

public interface ErrorCode {
    String name();
    int getStatus();
    String getMessage();
    String getCode();
}
