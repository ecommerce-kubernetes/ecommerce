package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode{
    CATEGORY_NOT_FOUND(404, "CATEGORY_001", "카테고리를 찾을 수 없습니다"),
    HAS_PRODUCT(409, "CATEGORY_002", "카테고리에 속한 상품이 존재합니다");
    private final int status;
    private final String code;
    private final String message;
}
