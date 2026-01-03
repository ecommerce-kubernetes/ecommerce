package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    CART_ITEM_MINIMUM_ONE_REQUIRED(400, "CART_001", "상품 수량은 한개 이상이여야 합니다"),
    CART_NO_PERMISSION(403, "CART_002", "장바구니에 접근할 권한이 없습니다"),
    CART_NOT_FOUND(404, "CART_003", "장바구니를 찾을 수 없습니다"),
    CART_ITEM_NOT_FOUND(404, "CART_004", "장바구니에서 해당 상품을 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
