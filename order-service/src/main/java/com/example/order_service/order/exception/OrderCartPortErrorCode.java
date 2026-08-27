package com.example.order_service.order.exception;

import com.example.order_service.common.exception.ErrorCategory;
import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCartPortErrorCode implements ErrorCode {
    CART_SERVER_ERROR(ErrorCategory.SYSTEM_ERROR, "CART_SERVER_ERROR", "장바구니 항목 조회중 에러가 발생했습니다.");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
