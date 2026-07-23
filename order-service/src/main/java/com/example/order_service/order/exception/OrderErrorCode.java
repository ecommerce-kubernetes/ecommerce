package com.example.order_service.order.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_USER_CLIENT_ERROR(HttpStatus.CONFLICT, "ORDER_USER_CLIENT_ERROR", "주문중 에러가 발생했습니다"),
    ORDER_USER_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_USER_SERVER_ERROR", "주문중 에러가 발생했습니다"),
    ORDER_USER_UNAVAILABLE_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "ORDER_USER_UNAVAILABLE_SERVER_ERROR", "주문중 일시적 에러가 발생했습니다 잠시후 다시 시도해주세요"),
    ORDER_USER_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "ORDER_USER_CIRCUIT_OPEN", "주문중 일시적 에러가 발생했습니다 잠시후 다시 시도해주세요"),

    ORDER_PRODUCT_CLIENT_ERROR(HttpStatus.CONFLICT, "ORDER_PRODUCT_CLIENT_ERROR", "주문중 에러가 발생했습니다"),
    ORDER_PRODUCT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_PRODUCT_SERVER_ERROR", "주문중 에러가 발생했습니다"),
    ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR", "주문중 일시적 에러가 발생했습니다 잠시후 다시 시도해주세요"),
    ORDER_PRODUCT_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "ORDER_PRODUCT_CIRCUIT_OPEN", "주문중 일시적인 에러가 발생했습니다 잠시후 다시 시도해주세요"),

    INVALID_ITEM_QUANTITY(HttpStatus.CONFLICT, "INVALID_ITEM_QUANTITY", "항목 수량이 유효하지 않습니다."),

    ORDER_ITEMS_DUPLICATE(HttpStatus.BAD_REQUEST, "ORDER_ITEMS_DUPLICATE", "중복된 상품 아이디가 존재합니다"),
    ORDER_ITEMS_REQUIRED(HttpStatus.CONFLICT, "ORDER_ITEMS_REQUIRED", "주문 상품은 최소 한개 이상이여야 합니다"),
    ORDER_COUPON_ITEM_NOT_IN_ITEMS(HttpStatus.BAD_REQUEST, "ORDER_COUPON_ITEM_NOT_IN_ITEMS", "쿠폰 적용 대상 상품이 주문 상품에 존재하지 않습니다"),
    ORDER_DUPLICATE_COUPON_APPLICATION(HttpStatus.BAD_REQUEST, "ORDER_DUPLICATE_COUPON_APPLICATION", "하나의 상품에는 하나의 쿠폰만 적용 가능합니다"),
    ORDER_ALREADY_APPLIED_TO_ANOTHER_ITEM(HttpStatus.BAD_REQUEST, "ORDER_ALREADY_APPLIED_TO_ANOTHER_ITEM", "하나의 쿠폰은 하나의 상품에만 적용 가능합니다"),
    INVALID_ORDER_STATUS_FOR_FAIL(HttpStatus.CONFLICT, "INVALID_ORDER_STATUS_FOR_FAIL", "주문을 실패 상태로 변경할 수 없습니다"),
    INVALID_ORDER_STATUS_FOR_COMPLETION(HttpStatus.CONFLICT, "INVALID_ORDER_STATUS_FOR_COMPLETION", "주문을 완료 상태로 변경할 수 없습니다"),
    INVALID_ORDER_STATUS_FOR_PAYMENT(HttpStatus.CONFLICT, "INVALID_ORDER_STATUS_FOR_PAYMENT", "결제 가능한 주문이 아닙니다"),
    ORDER_POINT_POLICY_VIOLATION(HttpStatus.CONFLICT, "ORDER_POINT_POLICY_VIOLATION", "현재 주문에 사용할 수 있는 포인트를 초과했습니다"),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "주문 조회 권한이 없습니다"),
    POINTS_DISCOUNT_CHANGE(HttpStatus.CONFLICT, "POINT_DISCOUNT_CHANGE", "사용 가능 포인트가 변동되었습니다"),
    ITEM_COUPON_DISCOUNT_CHANGE(HttpStatus.CONFLICT, "ITEM_COUPON_DISCOUNT_CHANGE", "상품 쿠폰 할인금이 변동되었습니다"),
    PRODUCT_PRICE_CHANGE(HttpStatus.CONFLICT, "PRODUCT_PRICE_CHANGE", "주문 상품 가격이 변동되었습니다"),
    ORDER_SHEET_EXPIRED(HttpStatus.CONFLICT, "ORDER_SHEET_EXPIRED", "주문서가 만료되었습니다"),
    INVALID_PHONE_NUMBER(HttpStatus.CONFLICT, "INVALID_PHONE_NUMBER", "유효하지 않은 전화번호 형식입니다."),
    INVALID_ZIPCODE(HttpStatus.CONFLICT, "INVALID_ZIPCODE", "유효하지 않은 우편번호 형식입니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_ITEM_NOT_FOUND", "주문 상품을 찾을 수 없습니다"),
    ORDER_SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_SHEET_NOT_FOUND", "주문서를 찾을 수 없습니다"),
    ORDER_SHEET_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_SHEET_ITEM_NOT_FOUND", "주문 항목을 찾을 수 없습니다."),
    CART_COUPON_MINIMUM_PAYMENT_NOT_MET(HttpStatus.CONFLICT, "CART_COUPON_MINIMUM_PAYMENT_NOT_MET", "장바구니 쿠폰의 최소 결제 금액을 만족하지 않습니다."),
    EXCEED_AVAILABLE_POINTS(HttpStatus.CONFLICT, "EXCEED_AVAILABLE_POINTS", "적용 가능한 포인트 한도를 초과했습니다."),
    ORDER_PRODUCT_INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "ORDER_PRODUCT_INSUFFICIENT_STOCK", "상품 수량이 부족합니다"),
    ORDER_PRODUCT_UNORDERABLE(HttpStatus.CONFLICT, "ORDER_PRODUCT_UNORDERABLE", "판매할 수 없는 상품이 존재합니다"),
    ORDER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_PRODUCT_NOT_FOUND", "주문 상품을 찾을 수 없습니다"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
