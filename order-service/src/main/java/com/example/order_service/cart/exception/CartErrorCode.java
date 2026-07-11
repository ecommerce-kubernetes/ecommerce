package com.example.order_service.cart.exception;

import com.example.order_service.common.exception.application.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_NOT_FOUND", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "장바구니에서 항목을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    CART_SIZE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "CART_SIZE_LIMIT_EXCEEDED", "장바구니 최대 항목 개수를 초과했습니다."),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.CONFLICT, "INVALID_CART_ITEM_QUANTITY", "항목 수량이 유효하지 않습니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.CONFLICT, "PRODUCT_NOT_ON_SALE", "판매중인 상품이 아닙니다."),

    CART_PRODUCT_CLIENT_ERROR(HttpStatus.CONFLICT, "CART_PRODUCT_CLIENT_ERROR", "상품 추가중 에러가 발생했습니다"),
    CART_PRODUCT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CART_PRODUCT_SERVER_ERROR", "상품 추가중 에러가 발생했습니다"),
    CART_PRODUCT_UNAVAILABLE_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "CART_PRODUCT_UNAVAILABLE_SERVER_ERROR", "상품 추가중 일시적인 에러가 발생했습니다 잠시후 다시 시도해주세요"),
    CART_PRODUCT_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "CART_PRODUCT_CIRCUIT_OPEN", "상품 추가중 일시적인 에러가 발생했습니다 잠시후 다시 시도해주세요");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
