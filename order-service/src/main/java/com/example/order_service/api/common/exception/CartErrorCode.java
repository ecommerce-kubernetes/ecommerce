package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    ORDER_ITEM_MINIMUM_ONE_REQUIRED(400, "CART_001", "상품 수량은 한개 이상이여야 합니다");

    private final int status;
    private final String code;
    private final String message;
}
