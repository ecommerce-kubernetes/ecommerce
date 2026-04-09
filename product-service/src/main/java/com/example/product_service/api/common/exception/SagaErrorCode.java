package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaErrorCode implements ErrorCode{
    STOCK_RESTORE_FAIL(500, "SAGA_001", "재고 복구에 실패하였습니다");
    private final int status;
    private final String code;
    private final String message;
}
