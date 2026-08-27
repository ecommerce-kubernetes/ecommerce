package com.example.order_service.order.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderCartPortErrorCode implements ErrorCode {
    CART_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CART_SERVER_ERROR", "장바구니 항목 조회중 에러가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
