package com.example.product_service.option.exception;

import com.example.product_service.common.exception.ErrorCategory;
import com.example.product_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OptionErrorCode implements ErrorCode {
    OPTION_TYPE_DUPLICATE_NAME(ErrorCategory.NOT_FOUND, "OPTION_TYPE_DUPLICATE_NAME", "동일한 옵션 이름이 존재합니다"),
    OPTION_VALUE_DUPLICATE_NAME(ErrorCategory.NOT_FOUND, "OPTION_VALUE_DUPLICATE_NAME", "동일한 옵션 값 이름을 설정할 수 없습니다"),
    OPTION_TYPE_NOT_FOUND(ErrorCategory.NOT_FOUND, "OPTION_TYPE_NOT_FOUND", "옵션 타입을 찾을 수 없습니다"),
    OPTION_TYPE_IN_PRODUCT(ErrorCategory.BUSINESS_CONFLICT, "OPTION_TYPE_IN_PRODUCT", "옵션 타입을 사용하는 상품이 존재합니다."),
    OPTION_VALUE_NOT_FOUND(ErrorCategory.NOT_FOUND, "OPTION_VALUE_NOT_FOUND", "옵션 값을 찾을 수 없습니다"),
    OPTION_VALUE_IN_PRODUCT(ErrorCategory.BUSINESS_CONFLICT, "OPTION_VALUE_IN_PRODUCT", "해당 옵션값이 상품 변형에 속해있습니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
