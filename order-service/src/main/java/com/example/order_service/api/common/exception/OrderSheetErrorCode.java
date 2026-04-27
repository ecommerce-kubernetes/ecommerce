package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSheetErrorCode implements ErrorCode {
    ORDER_SHEET_ITEM_REQUIRED(400, "ORDER-SHEET_002", "주문 상품은 필수 입니다"),
    ORDER_SHEET_DUPLICATE_ITEMS(400, "ORDER-SHEET_001", "중복된 상품 ID 가 존재합니다");

    private final int status;
    private final String code;
    private final String message;

}
