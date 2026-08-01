package com.example.order_service.order.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    INVALID_ITEM_QUANTITY(HttpStatus.CONFLICT, "INVALID_ITEM_QUANTITY", "항목 수량이 유효하지 않습니다."),
    ORDER_ITEMS_REQUIRED(HttpStatus.CONFLICT, "ORDER_ITEMS_REQUIRED", "주문 상품은 최소 한개 이상이여야 합니다"),
    ORDER_SHEET_EXPIRED(HttpStatus.CONFLICT, "ORDER_SHEET_EXPIRED", "주문서가 만료되었습니다"),
    INVALID_PHONE_NUMBER(HttpStatus.CONFLICT, "INVALID_PHONE_NUMBER", "유효하지 않은 전화번호 형식입니다."),
    INVALID_ZIPCODE(HttpStatus.CONFLICT, "INVALID_ZIPCODE", "유효하지 않은 우편번호 형식입니다."),
    INVALID_PRODUCT_DISCOUNT_RATE(HttpStatus.CONFLICT, "INVALID_PRODUCT_DISCOUNT_RATE", "유효하지 않은 상품 할인율 입니다."),
    INVALID_PRODUCT_DISCOUNT_AMOUNT(HttpStatus.CONFLICT, "INVALID_PRODUCT_DISCOUNT_AMOUNT", "유효하지 않은 상품 할인 금액 입니다."),
    INVALID_PRODUCT_DISCOUNTED_PRICE(HttpStatus.CONFLICT, "INVALID_PRODUCT_DISCOUNTED_PRICE", "유효하지 않은 상품 판매 가격 입니다."),
    INVALID_ORDER_ITEM_FINAL_AMOUNT(HttpStatus.CONFLICT, "INVALID_ORDER_ITEM_FINAL_AMOUNT", "유효하지 않은 주문 항목 최종 가격 입니다."),
    ITEM_DISCOUNT_EXCEEDS_ORIGINAL_AMOUNT(HttpStatus.CONFLICT, "ITEM_DISCOUNT_EXCEEDS_ORIGINAL_AMOUNT", "상품 원 가격 총액은 상품 할인 금액 총액보다 작을 수 없습니다."),
    INVALID_ORDER_ITEM_LINE_TOTAL(HttpStatus.CONFLICT, "INVALID_ORDER_ITEM_LINE_TOTAL", "유효하지 않은 상품 판매가 총액 입니다."),
    ITEM_COUPON_DISCOUNT_EXCEEDS_LINE_TOTAL(HttpStatus.CONFLICT, "ITEM_COUPON_DISCOUNT_EXCEEDS_LINE_TOTAL", "상품 판매가 총액은 상품 쿠폰 할인 금액보다 적을 수 없습니다."),
    ORDER_DISCOUNT_EXCEEDS_TOTAL_AMOUNT(HttpStatus.CONFLICT, "ORDER_DISCOUNT_EXCEEDS_TOTAL_AMOUNT", "총 할인 금액은 주문 원가를 초과할 수 없습니다."),
    INVALID_TOTAL_PAYMENT_AMOUNT(HttpStatus.CONFLICT, "INVALID_TOTAL_PAYMENT_AMOUNT", "유효하지 않은 최종 결제 금액 입니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "장바구니 항목을 찾을 수 없습니다"),
    ORDER_SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_SHEET_NOT_FOUND", "주문서를 찾을 수 없습니다"),
    ORDER_SHEET_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_SHEET_ITEM_NOT_FOUND", "주문 항목을 찾을 수 없습니다."),
    CART_COUPON_MINIMUM_PAYMENT_NOT_MET(HttpStatus.CONFLICT, "CART_COUPON_MINIMUM_PAYMENT_NOT_MET", "장바구니 쿠폰의 최소 결제 금액을 만족하지 않습니다."),
    EXCEED_AVAILABLE_POINTS(HttpStatus.CONFLICT, "EXCEED_AVAILABLE_POINTS", "적용 가능한 포인트 한도를 초과했습니다."),
    ORDER_PRODUCT_INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "ORDER_PRODUCT_INSUFFICIENT_STOCK", "상품 수량이 부족합니다"),
    ORDER_PRODUCT_UNORDERABLE(HttpStatus.CONFLICT, "ORDER_PRODUCT_UNORDERABLE", "판매할 수 없는 상품이 존재합니다"),
    ORDER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_PRODUCT_NOT_FOUND", "주문 상품을 찾을 수 없습니다"),
    PRODUCT_PRICE_CHANGED(HttpStatus.CONFLICT, "PRODUCT_PRICE_CHANGED", "상품 가격이 변동되었습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
