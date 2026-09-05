package com.example.product_service.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaErrorCode implements ErrorCode {
    STOCK_RESTORE_FAIL(ErrorCategory.SYSTEM_ERROR, "SAGA_001", "재고 복구에 실패하였습니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
