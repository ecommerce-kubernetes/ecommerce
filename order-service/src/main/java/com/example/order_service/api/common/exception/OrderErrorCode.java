package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "ORDER_001", "주문을 찾을 수 없습니다"),
    ORDER_NOT_PAYABLE(409, "ORDER_002", "결제할 수 없는 주문입니다"),
    ORDER_NO_PERMISSION(403, "ORDER_003", "주문에 접근할 권한이 없습니다"),
    ORDER_PRODUCT_NOT_FOUND(404, "ORDER_004", "주문 상품을 찾을 수 없습니다"),
    ORDER_PRODUCT_OUT_OF_STOCK(409, "ORDER_005", "상품 재고가 부족합니다"),
    ORDER_INSUFFICIENT_POINT_BALANCE(409, "ORDER_006", "포인트 잔액이 부족합니다"),
    ORDER_PRICE_MISMATCH(409, "ORDER_007", "주문 금액이 변동되었습니다"),
    ORDER_ITEM_MINIMUM_ONE_REQUIRED(400, "ORDER_008", "주문 상품은 1개 이상이여야 합니다");
    private final int status;
    private final String code;
    private final String message;
}
