package com.example.product_service.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OptionErrorCode implements ErrorCode {
    OPTION_NOT_FOUND(ErrorCategory.NOT_FOUND, "OPTION_001", "옵션을 찾을 수 없습니다"),
    DUPLICATE_NAME(ErrorCategory.NOT_FOUND, "OPTION_002", "동일한 옵션 이름이 존재합니다"),
    OPTION_IN_PRODUCT_OPTION(ErrorCategory.BUSINESS_CONFLICT, "OPTION_003", "옵션이 상품에 속해있습니다"),
    OPTION_VALUE_NOT_FOUND(ErrorCategory.NOT_FOUND, "OPTION_004", "옵션 값을 찾을 수 없습니다"),
    OPTION_VALUE_DUPLICATE_NAME(ErrorCategory.NOT_FOUND, "OPTION_005", "해당 옵션에 동일한 이름의 옵션 값이 있습니다"),
    OPTION_VALUE_IN_VARIANT(ErrorCategory.BUSINESS_CONFLICT, "OPTION_006", "해당 옵션값이 상품 변형에 속해있습니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
