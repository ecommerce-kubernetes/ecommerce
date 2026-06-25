package com.example.order_service.common.exception;

import com.example.order_service.common.exception.application.ErrorCode;

public interface ErrorTranslator {
    ErrorCode translate(String code);
}
