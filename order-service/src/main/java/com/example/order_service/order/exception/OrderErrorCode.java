package com.example.order_service.order.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    POINTS_DISCOUNT_CHANGE(409, "POINT_DISCOUNT_CHANGE", "사용 가능 포인트가 변동되었습니다"),
    ITEM_COUPON_DISCOUNT_CHANGE(409, "ITEM_COUPON_DISCOUNT_CHANGE", "상품 쿠폰 할인금이 변동되었습니다"),
    CART_COUPON_DISCOUNT_CHANGE(409, "CART_COUPON_DISCOUNT_CHANGE", "장바구니 상품 할인금이 변동되었습니다"),
    PRODUCT_PRICE_CHANGE(409, "PRODUCT_PRICE_CHANGE", "주문 상품 가격이 변동되었습니다"),
    ORDER_SHEET_EXPIRED(409, "ORDER_SHEET_ACCESS_DENIED", "주문이 만료되었습니다"),
    ORDER_SHEET_ACCESS_DENIED(403, "ORDER_SHEET_ACCESS_DENIED", "주문 권한이 없습니다"),
    ORDER_SHEET_NOT_FOUND(404, "ORDER-SHEET_NOT_FOUND", "주문서를 찾을 수 없습니다"),
    ORDER_NOT_FOUND(404, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
