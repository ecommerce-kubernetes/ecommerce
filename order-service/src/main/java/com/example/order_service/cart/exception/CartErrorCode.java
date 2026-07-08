package com.example.order_service.cart.exception;

import com.example.order_service.common.exception.application.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    CART_PRODUCT_CLIENT_ERROR(HttpStatus.CONFLICT, "CART_PRODUCT_CLIENT_ERROR", "상품 추가중 에러가 발생했습니다"),
    CART_PRODUCT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CART_PRODUCT_SERVER_ERROR", "상품 추가중 에러가 발생했습니다"),
    CART_PRODUCT_UNAVAILABLE_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "CART_PRODUCT_UNAVAILABLE_SERVER_ERROR", "상품 추가중 일시적인 에러가 발생했습니다 잠시후 다시 시도해주세요"),
    CART_PRODUCT_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "CART_PRODUCT_CIRCUIT_OPEN", "상품 추가중 일시적인 에러가 발생했습니다 잠시후 다시 시도해주세요"),

    CART_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_PRODUCT_NOT_FOUND", "상품 정보를 찾을 수 없습니다"),
    CART_PRODUCT_CANNOT_ADD(HttpStatus.NOT_FOUND, "CART_PRODUCT_CANNOT_ADD", "장바구니에 추가할 수 없는 상품이 있습니다"),
    CART_PRODUCT_STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "CART_PRODUCT_STOCK_INSUFFICIENT", "수량이 부족합니다"),

    EXCEED_AVAILABLE_CART_SIZE(HttpStatus.CONFLICT, "EXCEED_AVAILABLE_CART_SIZE", "장바구니 추가 수량을 초과했습니다"),

    CART_ITEM_MINIMUM_ONE_REQUIRED(HttpStatus.BAD_REQUEST, "CART_001", "상품 수량은 한개 이상이여야 합니다"),
    CART_NO_PERMISSION(HttpStatus.FORBIDDEN, "CART_002", "장바구니에 접근할 권한이 없습니다"),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_003", "장바구니를 찾을 수 없습니다"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_004", "장바구니에서 해당 상품을 찾을 수 없습니다");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
