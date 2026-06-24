package com.example.order_service.cart.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    CART_ITEM_MINIMUM_ONE_REQUIRED(HttpStatus.BAD_REQUEST, "CART_001", "상품 수량은 한개 이상이여야 합니다"),
    CART_NO_PERMISSION(HttpStatus.FORBIDDEN, "CART_002", "장바구니에 접근할 권한이 없습니다"),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_003", "장바구니를 찾을 수 없습니다"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_004", "장바구니에서 해당 상품을 찾을 수 없습니다"),
    CART_PRODUCT_CANNOT_ADD(HttpStatus.NOT_FOUND, "CART_005", "장바구니에 추가할 수 없는 상품이 있습니다"),
    CART_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_006", "존재하지 않은 상품은 장바구니에 담을 수 없습니다");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
