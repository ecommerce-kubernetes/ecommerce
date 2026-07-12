package com.example.order_service.common.exception;

public interface ErrorTranslator {
    ErrorCode translate(String code);
}
