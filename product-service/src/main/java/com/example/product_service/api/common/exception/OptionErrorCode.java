package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OptionErrorCode implements ErrorCode{
    OPTION_NOT_FOUND(404, "OPTION_001", "옵션을 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
