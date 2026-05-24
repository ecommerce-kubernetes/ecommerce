package com.example.order_service.order.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_SHEET_EXPIRED(409, "ORDER_SHEET_ACCESS_DENIED", "주문이 만료되었습니다"),
    ORDER_SHEET_ACCESS_DENIED(403, "ORDER_SHEET_ACCESS_DENIED", "주문 권한이 없습니다"),
    ORDER_SHEET_NOT_FOUND(404, "ORDER-SHEET_NOT_FOUND", "주문서를 찾을 수 없습니다"),
    ORDER_PAYMENT_UNAVAILABLE_SERVER_ERROR(503, "ORDER_022", "결제 승인 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    ORDER_PAYMENT_SERVER_ERROR(500, "ORDER_021", "결제 승인 처리중 서버 에러가 발생했습니다"),
    ORDER_PAYMENT_CLIENT_ERROR(409, "ORDER_020", "결제 승인 처리중 클라이언트 에러가 발생했습니다"),
    ORDER_COUPON_UNAVAILABLE_SERVER_ERROR(503, "ORDER_019", "주문 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    ORDER_COUPON_SERVER_ERROR(500, "ORDER_018", "주문 처리중 오류가 발생했습니다"),
    ORDER_COUPON_CLIENT_ERROR(409, "ORDER_017", "주문 처리중 클라이언트 오류가 발생했습니다"),
    ORDER_USER_UNAVAILABLE_SERVER_ERROR(503, "ORDER_016", "주문 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    ORDER_USER_SERVER_ERROR(500, "ORDER-015", "주문 처리중 오류가 발생했습니다"),
    ORDER_USER_CLIENT_ERROR(409, "ORDER-014", "주문 처리중 클라이언트 오류가 발생했습니다"),
    ORDER_PRODUCT_UNAVAILABLE_SERVER_ERROR(503, "ORDER_013", "주문 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    ORDER_PRODUCT_SERVER_ERROR(500, "ORDER_012", "주문 처리중 오류가 발생했습니다"),
    ORDER_PRODUCT_CLIENT_ERROR(409, "ORDER_011", "주문 처리중 클라이언트 오류가 발생했습니다"),
    ORDER_NOT_FOUND(404, "ORDER_001", "주문을 찾을 수 없습니다"),
    ORDER_NOT_PAYABLE(409, "ORDER_002", "결제할 수 없는 주문입니다"),
    ORDER_NO_PERMISSION(403, "ORDER_003", "주문에 접근할 권한이 없습니다"),
    ORDER_PRICE_MISMATCH(409, "ORDER_007", "주문 금액이 변동되었습니다"),
    ORDER_ITEM_MINIMUM_ONE_REQUIRED(400, "ORDER_008", "주문 상품은 1개 이상이여야 합니다");
    private final int status;
    private final String code;
    private final String message;
}
