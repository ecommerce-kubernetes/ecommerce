package com.example.order_service.ordersheet.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSheetErrorCode implements ErrorCode {
    ORDER_SHEET_ITEMS_REQUIRED(400, "ORDER_SHEET_ITEMS_REQUIRED", "주문 상품은 필수 입니다"),
    ORDER_SHEET_ITEMS_DUPLICATE(400, "ORDER_SHEET_ITEMS_DUPLICATE", "중복된 상품 아이디가 존재합니다"),
    ORDER_SHEET_COUPON_ITEM_NOT_IN_ITEMS(400, "ORDER_SHEET_COUPON_ITEM_NOT_IN_ITEMS", "쿠폰 적용 대상 상품이 주문 상품에 존재하지 않습니다"),
    ORDER_SHEET_DUPLICATE_COUPON_APPLICATION(400, "ORDER_SHEET_DUPLICATE_COUPON_APPLICATION", "하나의 상품에는 하나의 쿠폰만 적용 가능합니다"),
    ORDER_SHEET_ALREADY_APPLIED_TO_ANOTHER_ITEM(400, "ORDER_SHEET_ALREADY_APPLIED_TO_ANOTHER_ITEM", "하나의 쿠폰은 하나의 상품에만 적용 가능합니다"),

    ORDER_SHEET_COUPON_UNAVAILABLE_SERVER_ERROR(503, "ORDER-SHEET-013", "주문 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    ORDER_SHEET_COUPON_SERVER_ERROR(500,"ORDER-SHEET_012", "주문 처리중 오류가 발생했습니다"),
    ORDER_SHEET_COUPON_CLIENT_ERROR(409, "ORDER-SHEET_011", "주문 처리중 클라이언트 오류가 발생했습니다"),
    ORDER_SHEET_NO_PERMISSION(403, "ORDER-SHEET-010", "주문서에 대한 접근 권한이 없습니다"),
    ORDER_SHEET_NOT_FOUND(404, "ORDER-SHEET-009", "주문서를 찾을 수 없습니다"),
    ORDER_SHEET_PRODUCT_NOT_FOUND(404, "ORDER-SHEET-008", "주문 상품을 찾을 수 없습니다"),
    ORDER_SHEET_PRODUCT_UNAVAILABLE_SERVER_ERROR(503, "ORDER-SHEET-007", "주문 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    // 해당 코드(PRODUCT_CLIENT_ERROR)는 상품 서비스에서 발생한 400번대의 오류를 처리하기 위한 임시 에러 코드 (기능 확장 시 코드 추가하여 매핑)
    ORDER_SHEET_PRODUCT_CLIENT_ERROR(409, "ORDER-SHEET_006", "주문 처리중 클라이언트 오류가 발생했습니다"),
    ORDER_SHEET_PRODUCT_SERVER_ERROR(500, "ORDER-SHEET_005", "주문 처리중 오류가 발생했습니다"),
    ORDER_SHEET_PRODUCT_UNORDERABLE(409, "ORDER-SHEET_004", "주문할 수 없는 상품이 포함되어있습니다"),
    ORDER_SHEET_INSUFFICIENT_STOCK(409, "ORDER-SHEET_003", "상품 재고가 부족합니다");

    private final int status;
    private final String code;
    private final String message;

}
