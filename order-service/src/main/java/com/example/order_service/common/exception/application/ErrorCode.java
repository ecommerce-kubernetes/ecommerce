package com.example.order_service.common.exception.application;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();
    HttpStatus getStatus();
    String getMessage();
    String getCode();
}
