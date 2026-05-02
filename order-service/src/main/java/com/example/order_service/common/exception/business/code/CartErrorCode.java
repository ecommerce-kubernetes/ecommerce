package com.example.order_service.common.exception.business.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    CART_PRODUCT_UNAVAILABLE_SERVER_ERROR(503, "CART_009", "처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    CART_PRODUCT_SERVER_ERROR(500, "CART_008", "처리중 오류가 발생했습니다"),
    CART_PRODUCT_CLIENT_ERROR(409, "CART_007", "처리중 클라이언트 오류가 발생했습니다"),
    CART_ITEM_MINIMUM_ONE_REQUIRED(400, "CART_001", "상품 수량은 한개 이상이여야 합니다"),
    CART_NO_PERMISSION(403, "CART_002", "장바구니에 접근할 권한이 없습니다"),
    CART_NOT_FOUND(404, "CART_003", "장바구니를 찾을 수 없습니다"),
    CART_ITEM_NOT_FOUND(404, "CART_004", "장바구니에서 해당 상품을 찾을 수 없습니다"),
    CART_PRODUCT_CANNOT_ADD(404, "CART_005", "장바구니에 추가할 수 없는 상품이 있습니다"),
    CART_PRODUCT_NOT_FOUND(404, "CART_006", "존재하지 않은 상품은 장바구니에 담을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
