package com.example.order_service.cart.exception;

import com.example.order_service.common.exception.ErrorCategory;
import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    CART_NOT_FOUND(ErrorCategory.NOT_FOUND, "CART_NOT_FOUND", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(ErrorCategory.NOT_FOUND, "CART_ITEM_NOT_FOUND", "장바구니에서 항목을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(ErrorCategory.NOT_FOUND, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    CART_SIZE_LIMIT_EXCEEDED(ErrorCategory.BUSINESS_CONFLICT, "CART_SIZE_LIMIT_EXCEEDED", "장바구니 최대 항목 개수를 초과했습니다."),
    INVALID_CART_ITEM_QUANTITY(ErrorCategory.BUSINESS_CONFLICT, "INVALID_CART_ITEM_QUANTITY", "항목 수량이 유효하지 않습니다."),
    QUANTITY_EXCEED_MAX_LIMIT(ErrorCategory.BUSINESS_CONFLICT, "QUANTITY_EXCEED_MAX_LIMIT", "수량이 한도를 초과했습니다."),
    CART_ITEMS_REQUIRED(ErrorCategory.BUSINESS_CONFLICT, "CART_ITEMS_REQUIRED", "장바구니에 추가할 상품은 최소 한개 이상이여야 합니다."),
    PRODUCT_NOT_ON_SALE(ErrorCategory.BUSINESS_CONFLICT, "PRODUCT_NOT_ON_SALE", "판매중인 상품이 아닙니다.");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
